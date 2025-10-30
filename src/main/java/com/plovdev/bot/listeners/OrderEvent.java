package com.plovdev.bot.listeners;

import com.plovdev.bot.modules.beerjes.Order;

public interface OrderEvent {
    void onOrder(Order order);
}