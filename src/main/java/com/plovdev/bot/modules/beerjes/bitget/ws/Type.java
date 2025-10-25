package com.plovdev.bot.modules.beerjes.bitget.ws;

public enum Type {
    //Futures
    USDT_FUTURES("USDT-FUTURES"),
    MC("MC"),
    UMCBL("UMCBL"),
    //Spot
    SP("SP"),
    SPBL("SPBL"),
    SPOT("SPOT");

    private final String type;
    Type(String tp) {
        type = tp;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Type: [" + type + "]";
    }
}