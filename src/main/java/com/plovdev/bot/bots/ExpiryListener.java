package com.plovdev.bot.bots;

import java.util.ArrayList;
import java.util.List;

public class ExpiryListener {
    private static final List<ExpiryEvent> events = new ArrayList<>();

    public static void addListener(ExpiryEvent event) {
        events.add(event);
    }
    public static void removeListener(ExpiryEvent event) {
        events.remove(event);
    }

    public static void notifyListeners(String id, String lang) {
        events.forEach(e -> e.expireid(id, lang));
    }
}