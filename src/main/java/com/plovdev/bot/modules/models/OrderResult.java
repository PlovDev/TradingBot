package com.plovdev.bot.modules.models;

import java.util.List;

public record OrderResult(boolean succes, String id, String pair, String message, List<String> warns, List<String> allResult) {
    public static OrderResult ok(String msg, String i, String p) {
        return new OrderResult(true, i, p, msg, List.of(), List.of(msg, "0"));
    }
    public static OrderResult error(String msg, String i, String p) {
        return new OrderResult(false, msg, i, p, List.of(), List.of(msg, "1"));
    }
    public static OrderResult no() {
        return new OrderResult(false,  "none", "undefine","ignored order operation", List.of(), List.of("-1"));
    }
}