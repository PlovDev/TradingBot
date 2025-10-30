package com.plovdev.bot.listeners;

import com.plovdev.bot.modules.beerjes.Position;

public interface PositionEvent {
    void onPositionOpened(Position position);
}