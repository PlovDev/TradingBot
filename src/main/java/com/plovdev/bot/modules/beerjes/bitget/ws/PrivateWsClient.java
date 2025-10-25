package com.plovdev.bot.modules.beerjes.bitget.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.plovdev.bot.bots.EnvReader;
import com.plovdev.bot.modules.beerjes.security.BitGetSecurity;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.exceptions.ApiException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.plovdev.bot.modules.beerjes.bitget.BitGetPaths.*;

public class PrivateWsClient extends WebSocketClient {
    private final Logger logger = LoggerFactory.getLogger("PrivateWsClient");
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final BitGetSecurity security = new BitGetSecurity(EnvReader.getEnv("bitgetPassword"));
    private ScheduledExecutorService pinger = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    private boolean isNeedLogin = true;
    private boolean isLogined = false;
    private boolean isConnected = false;

    public boolean isLogined() {
        return isLogined;
    }

    public boolean isConnected() {
        return isConnected;
    }

    private UserEntity user;
    private BitGetWSListener listener;

    public PrivateWsClient(UserEntity bitgetuser) {
        super(URI.create(WS_BASE + WS_STREAM));
        logger.info("Initing private bitget client");
        if (!bitgetuser.getBeerj().equalsIgnoreCase("bitget")) {
            throw new ApiException("Not a bitget user");
        }
        user = bitgetuser;
        logger.info("Private bitget client inited");
    }

    public BitGetWSListener getListener() {
        return listener;
    }

    public void setListener(BitGetWSListener listener) {
        this.listener = listener;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        if (!user.getBeerj().equalsIgnoreCase("bitget")) {
            throw new ApiException("Not a bitget user");
        }
        this.user = user;
    }

    public void connectToBitget(BitGetWSListener listener) {
        this.listener = listener;
        connect();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        short status = serverHandshake.getHttpStatus();
        String msg = serverHandshake.getHttpStatusMessage();
        logger.info("WebSocket open. Status: {}. Message: {}", status, msg);
        isConnected = true;
        tryLogin();
    }

    @Override
    public void onMessage(String s) {
        logger.info("Response: {}", s);

        if (s != null && !s.isEmpty()) {
            if (s.equals("pong")) return;

            Channel channel = BitGetWSUtils.getAction(s);
            switch (channel) {
                case ORDER -> {
                    logger.info("Order channel response.");
                    listener.onOrder(BitGetWSUtils.getOrder(s));
                }
                case ACCOUNT -> {
                    logger.info("Account channel response.");
                    listener.onAccount(BitGetWSUtils.getAccount(s));
                }
                case POSITION -> {
                    logger.info("Position channel response.");
                    listener.onPosition(BitGetWSUtils.getPosition(s));
                }
                case FILL -> {
                    logger.info("Fill channel response.");
                    listener.onFill(BitGetWSUtils.getOrder(s));
                }
                case POSITION_HISTORY -> {
                    logger.info("Position history channel response.");
                    listener.onPositionHistory(BitGetWSUtils.getPosition(s));
                }
                case PLACE_ORDER -> {
                    logger.info("Place order channel response.");
                    listener.onPlaceOrder(BitGetWSUtils.getOrder(s));
                }
                case CANCEL_ORDER -> {
                    logger.info("Cancel order channel response.");
                    listener.onCancelOrder(BitGetWSUtils.getOrder(s));
                }
                case ALG_ORDER -> {
                    logger.info("Alg orders channel response.");
                    listener.onOrderAlgo(BitGetWSUtils.getOrder(s));
                }

                case LOGIN -> {
                    logger.info("Login data pushed.");
                    WSResult loginResult = BitGetWSUtils.getResult(s);
                    if (loginResult.success()) {
                        isLogined = true;
                        sendAutoPing();
                        listener.onLogined(loginResult);
                    }
                }
                case SNAPSHOT -> {
                    logger.info("Snapshot response.");
                    listener.onSnaphot(s);
                }
                case UPDATE -> {
                    logger.info("Update response.");
                    listener.onUpdate(s);
                }
                case ERROR -> {
                    if (isNeedLogin) {
                        logger.warn("Can't login");
                        isLogined = false;
                    }

                    logger.info("Error response.");
                    WSResult err = BitGetWSUtils.getResult(s);
                    listener.onError(err);
                }
                case SUBSCRIBE -> {
                    logger.info("Subscribe response.");
                    listener.onSubscride(BitGetWSUtils.getSubscribeResult(s));
                }
                case UNSUBSCRIBE -> {
                    logger.info("Unsubscribe response.");
                    listener.onUnsubscribe(BitGetWSUtils.getSubscribeResult(s));
                }

                default -> {
                    logger.info("Default response.");
                    listener.onDefault(s);
                }
            }
        }
    }

    @Override
    public void onClose(int code, String s, boolean b) {
        isConnected = false;
        logger.warn("Connection closed: {} (code: {})", s, code);
        if (code != 1000) {
            logger.info("Try reconnect");
            reconnect();
        } else {
            cleanup();
        }
    }

    @Override
    public void onError(Exception e) {
        isConnected = false;
        logger.error("WebSocket error: ", e);
        cleanup();
    }

    public synchronized void tryLogin() {
        try {
            logger.info("Try login.");
            Map<String, Object> loginData = new HashMap<>();
            loginData.put("op", Channel.LOGIN.getName());

            String timestamp = String.valueOf(System.currentTimeMillis());

            String api = security.decrypt(user.getApiKey());
            String secret = security.decrypt(user.getSecretKey());
            String phrase = security.decrypt(user.getPhrase());
            String sign = security.generateSignature(timestamp + GET + WS_VERIFY, secret);

            Map<String, String> authData = new HashMap<>();
            authData.put("apiKey", api);
            authData.put("passphrase", phrase);
            authData.put("timestamp", timestamp);
            authData.put("sign", sign);

            List<Map<String, String>> args = new ArrayList<>();
            args.add(authData);
            loginData.put("args", args);

            String readyPayload = jsonMapper.writeValueAsString(loginData);
            sendToBitGet(readyPayload);

            isNeedLogin = false;
        } catch (Exception e) {
            logger.error("Login error: ", e);
        }
    }

    private void sendAutoPing() {
        pinger.scheduleAtFixedRate(() -> {
            sendToBitGet("ping");
            System.out.println("Sended ping");
        }, 5, 30, TimeUnit.SECONDS);
    }

    private synchronized void sendToBitGet(String message) {
        if (message != null && !message.isEmpty() && isConnected) {
            send(message);
        } else {
            logger.warn("Message is empty!");
        }
    }

    private void cleanup() {
        pinger.shutdownNow();
        executor.shutdownNow();
    }



    /**
     * Автопереподключение
     */
    @Override
    public void reconnect() {
        executor.execute(() -> {
            try {
                logger.info("Attempting to reconnect...");
                if (reconnectBlocking()) {
                    logger.info("Reconnection successful");
                    resetState();
                } else {
                    logger.warn("Cann't reconect.");
                }
            } catch (Exception e) {
                logger.error("Reconnection failed: ", e);
            }
        });
    }
    private void resetState() {
        if (pinger.isShutdown() || pinger.isTerminated()) pinger = Executors.newSingleThreadScheduledExecutor();
    }


    public void subscribe(String symbol, Type type, Channel channel) {
        String payload = formSubscribeData(symbol, type, channel, "subscribe");
        if (payload != null) {
            if (isConnected) {
                sendToBitGet(payload);
            } else {
                logger.warn("WebSocket to subscribe not connected, try later");
            }
        } else {
            logger.warn("Subscribe payload empty");
        }
    }
    private void unsubscribe(String symbol, Type type, Channel channel) {
        String payload = formSubscribeData(symbol, type, channel, "unsubscribe");
        if (payload != null) {
            if (isConnected) {
                sendToBitGet(payload);
            } else {
                logger.warn("WebSocket to unsubscribe not connected, try later");
            }
        } else {
            logger.warn("Unsubscribe payload empty");
        }
    }

    private String formSubscribeData(String symbol, Type type, Channel channel, String action) {
        if (symbol == null || symbol.isEmpty()) {
            symbol = "default";
        } else {
            symbol = symbol.toUpperCase().replace("USDT", "");
            if (!symbol.endsWith("USDT_UMCBL")) {
                symbol += "USDT_UMCBL";
            }
        }

        if (type == null || channel == null) {
            logger.warn("Empty data to {}", action);
            return null;
        }

        JsonObject subscribe = new JsonObject();
        subscribe.addProperty("op", action);

        JsonObject subscribeData = new JsonObject();
        subscribeData.addProperty("instType", type.getType());
        subscribeData.addProperty("channel", channel.getName());
        subscribeData.addProperty("instId", symbol);

        JsonArray args = new JsonArray();
        args.add(subscribeData);
        subscribe.add("args", args);

        return gson.toJson(subscribe);
    }
}