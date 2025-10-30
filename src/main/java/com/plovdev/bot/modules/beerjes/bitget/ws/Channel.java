package com.plovdev.bot.modules.beerjes.bitget.ws;

public enum Channel {
    ORDER("orders"),
    ACCOUNT("account"),
    POSITION("positions"),
    FILL("fill"),
    POSITION_HISTORY("positions-history"),
    PLACE_ORDER("place-order"),
    CANCEL_ORDER("cancel-order"),
    ALG_ORDER("orders-algo"),

    TICKER("ticker"),
    CANDLE("candle"),
    DEPTH("books"),
    TRADE("trade"),


    LOGIN("login"),
    SNAPSHOT("snapshot"),
    UPDATE("update"),
    ERROR("error"),
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe"),
    UNKNOWN("unknown");

    public String getName() {
        return name;
    }

    public static Channel of(String name) {
        for (Channel ch : Channel.values()) {
            if (ch.getName().toLowerCase().contains(name.toLowerCase())) {
                return ch;
            }
        }
        return Channel.UNKNOWN;
    }

    private final String name;

    Channel(String ch) {
        name = ch;
    }

    @Override
    public String toString() {
        return "CHANNEL: [" + name + "]";
    }
}