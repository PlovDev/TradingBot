package com.plovdev.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plovdev.bot.modules.beerjes.utils.BitUnixUtils;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WS {
    private final Logger log = LoggerFactory.getLogger("WS");
    private final ObjectMapper mapper = new ObjectMapper();

    private final String apiKey;
    private final String apiSecret;
    private final String wsUrl;

    private final OkHttpClient okHttpClient;
    private WebSocket webSocket;
    private boolean active = false;
    private boolean loginCompleted = false;

    // Поток для отправки ping
    private final ExecutorService pingExecutor = Executors.newSingleThreadExecutor();

    // Listener для обработки событий (можно сделать интерфейс, но для простоты — лямбды или внутренние методы)
    public interface Listener {
        void onBalanceUpdate(String data);

        void onPositionUpdate(String data);

        void onOrderUpdate(String data);

        void onTpslUpdate(String data);

        void onConnected();

        void onLoginSuccess();

        void onError(String message, Throwable t);
    }

    private final Listener listener;

    public WS(String apiKey, String apiSecret, Listener listener) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.wsUrl = "wss://fapi.bitunix.com/private";
        this.listener = listener;

        // OkHttpClient БЕЗ readTimeout и callTimeout — критично для WebSocket!
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .pingInterval(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(10))
                // НЕ УСТАНАВЛИВАЕМ readTimeout и callTimeout!
                .build();
    }

    public void connect() {
        log.info("Connecting to Bitunix Private WebSocket: " + wsUrl);

        Request request = new Request.Builder().url(wsUrl).build();

        WebSocketListener wsListener = new WebSocketListener() {
            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                log.info("WebSocket opened");
                WS.this.webSocket = webSocket;
                active = true;
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                try {
                    System.out.println("<< IN: " + text);
                    JSONObject object = new JSONObject(text);
                    String op = object.optString("op", object.optString("ch")).toLowerCase();

                    switch (op) {
                        case "connect" -> {
                            log.info("Received 'connect', authenticating...");
                            login();
                            return;
                        }
                        case "login" -> {
                            log.info("Login successful");
                            loginCompleted = true;
                            listener.onLoginSuccess();
                            startPing();
                            subscribeToChannels();
                            return;
                        }
                        case "pong" -> {
                            System.out.println("Received pong");
                            return;
                        }
                        default -> System.out.println("unknown chanel");
                    }

                    switch (op) {
                        case "balance" -> listener.onBalanceUpdate(text);
                        case "position" -> listener.onPositionUpdate(text);
                        case "order" -> listener.onOrderUpdate(text);
                        case "tpsl" -> listener.onTpslUpdate(text);
                        default -> log.warn("Unknown channel: {}", op);
                    }

                } catch (Exception e) {
                    log.error("Error handling message: {}", text, e);
                    listener.onError("Message handling failed", e);
                }
            }

            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                log.info("WebSocket closed: " + code + " - " + reason);
                cleanup();
                scheduleReconnect();
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                log.warn("WebSocket failure", t);
                listener.onError("WebSocket connection failed", t);
                cleanup();
                scheduleReconnect();
            }
        };

        this.webSocket = okHttpClient.newWebSocket(request, wsListener);
    }

    private void login() {
        try {
            long timestamp = System.currentTimeMillis();
            String nonce = "defaultnonce"; // как в оригинале

            // Строка для подписи: apiKey + nonce + timestamp + ""
            String signStr = apiKey + nonce + timestamp;
            String signature = BitUnixUtils.generateSign(nonce, String.valueOf(timestamp), apiKey, new TreeMap<>(), "", apiSecret);

            String loginJson = mapper.writeValueAsString(Map.of(
                    "op", "login",
                    "args", List.of(Map.of(
                            "apiKey", apiKey,
                            "nonce", nonce,
                            "timestamp", timestamp,
                            "sign", signature
                    ))
            ));

            log.info("Sending login: " + loginJson);
            webSocket.send(loginJson);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void subscribeToChannels() {
        try {
            String subscribeJson = mapper.writeValueAsString(Map.of(
                    "op", "subscribe",
                    "args", Arrays.asList("balance", "position", "order", "tpsl")
            ));
            log.info("Subscribing to private channels");
            webSocket.send(subscribeJson);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void startPing() {
        pingExecutor.execute(() -> {
            while (active) {
                try {
                    if (webSocket != null) {
                        String pingMsg = mapper.writeValueAsString(Map.of("op", "ping", "ts", Instant.now().getEpochSecond()));
                        webSocket.send(pingMsg);
                        System.out.println("Sent ping");
                    }
                    Thread.sleep(10_000); // 10 секунд
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.out.println("Ping failed" + e);
                    break;
                }
            }
        });
    }

    private void scheduleReconnect() {
        if (!active) {
            log.info("Scheduling reconnect in 5 seconds...");
            Executors.newSingleThreadScheduledExecutor().schedule(this::reconnect, 5, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    private void reconnect() {
        log.info("Reconnecting...");
        cleanup();
        connect();
    }

    private void cleanup() {
        active = false;
        loginCompleted = false;
        if (webSocket != null) {
            try {
                webSocket.close(1000, "Reconnect");
            } catch (Exception e) {
                log.error("Error closing WebSocket", e);
            }
            webSocket = null;
        }
    }

    public void disconnect() {
        cleanup();
        pingExecutor.shutdownNow();
    }
}