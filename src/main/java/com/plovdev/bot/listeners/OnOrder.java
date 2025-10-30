package com.plovdev.bot.listeners;

import com.plovdev.bot.modules.models.OrderResult;

public interface OnOrder {
    OrderResult onReady();
}