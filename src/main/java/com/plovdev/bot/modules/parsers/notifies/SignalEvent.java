package com.plovdev.bot.modules.parsers.notifies;

import com.plovdev.bot.modules.parsers.Signal;

@FunctionalInterface
public interface SignalEvent {
    void onSignal(Signal signal);
}