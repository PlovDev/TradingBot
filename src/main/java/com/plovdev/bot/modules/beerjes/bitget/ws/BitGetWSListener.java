package com.plovdev.bot.modules.beerjes.bitget.ws;

import com.plovdev.bot.modules.beerjes.Order;
import com.plovdev.bot.modules.beerjes.Position;
import com.plovdev.bot.modules.beerjes.bitget.Account;

public interface BitGetWSListener {
    void onOrder(Order order);
    void onAccount(Account account);
    void onPosition(Position position);
    void onFill(Order filled);
    void onPositionHistory(Position historied);
    void onPlaceOrder(Order place);
    void onCancelOrder(Order cancel);
    void onOrderAlgo(Order pushed);

    void onLogined(WSResult result);
    void onSnaphot(String json);
    void onUpdate(String json);
    void onError(WSResult result);
    void onSubscride(SubscribeResult result);
    void onUnsubscribe(SubscribeResult result);

    void onDefault(String json);
}