package com.plovdev.bot.modules.beerjes.bitget.ws;

public record WSResult(boolean success, int code, String msg) {
    public static WSResult error(int code, String msg) {
        return new WSResult(false, code, msg);
    }
    public static WSResult ok(int code, String msg) {
        return new WSResult(true, code, msg);
    }
    public static WSResult ok() {
        return new WSResult(true, 0, "OK");
    }
    public static WSResult successful(String msg) {
        return new WSResult(true, 0, msg);
    }
    public static WSResult successful() {
        return new WSResult(true, 0, "Success");
    }

    public static WSResult no() {
        return new WSResult(false, -1, "Undefiend");
    }
}