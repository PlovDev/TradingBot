package com.plovdev.bot.modules.parsers.notifies;

import com.plovdev.bot.modules.parsers.Signal;

import java.util.ArrayList;
import java.util.List;

public class SignalListener {
    private static final List<SignalEvent> events = new ArrayList<>();

    private SignalListener() {}

    public static void addSignalListener(SignalEvent event) {
        events.add(event);
    }
    public static void removeSignalListener(SignalEvent event) {
        events.remove(event);
    }

    public static void notifySignals(Signal signal) {
        for (SignalEvent event : events) event.onSignal(signal);
    }
}
