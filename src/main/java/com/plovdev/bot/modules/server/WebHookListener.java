package com.plovdev.bot.modules.server;

import com.plovdev.bot.modules.parsers.TradingViewSignalParser;
import com.plovdev.bot.modules.parsers.notifies.SignalListener;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebHookListener {
    public static final int PORT = 8080;
    private boolean isRunning = false;
    private HttpServer server;
    private final Logger logger = LoggerFactory.getLogger("Server");

    public void startServer() {
        if (isRunning) {
            logger.info("Webhook server already running");
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/tradingbot/webhook", exchange -> {
                String body = new String(exchange.getRequestBody().readAllBytes());
                logger.info("Getted tv body:");
                System.out.println(body);
                String response = "{\"status\":\"ok\",\"service\":\"tradingview-webhook\"}";
                sendResponse(exchange, 200, response, "application/json");
                if (TradingViewSignalParser.validate(body)) {
                    SignalListener.notifySignals(TradingViewSignalParser.parse(body));
                }
            });
            server.createContext("/health", exchange -> {
                String response = "{\"status\":\"ok\",\"service\":\"tradingview-webhook\"}";
                sendResponse(exchange, 200, response, "application/json");
            });

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();

            isRunning = true;
            logger.info("✅ TradingView Webhook server started on port {}", PORT);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Server start error.");
        }
    }

    public void stopWebhookServer() {
        if (server != null) {
            server.stop(0);
            isRunning = false;
            logger.info("🛑 Webhook server stopped");
        }
    }

    private void sendResponse(HttpExchange exchange, int code, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(code, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }



    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}