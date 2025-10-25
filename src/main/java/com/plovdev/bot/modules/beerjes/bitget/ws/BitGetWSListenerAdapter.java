package com.plovdev.bot.modules.beerjes.bitget.ws;

import com.plovdev.bot.modules.beerjes.Order;
import com.plovdev.bot.modules.beerjes.Position;
import com.plovdev.bot.modules.beerjes.bitget.Account;

public abstract class BitGetWSListenerAdapter implements BitGetWSListener {
    @Override
    public void onOrder(Order order) {

    }

    @Override
    public void onAccount(Account account) {

    }

    @Override
    public void onPosition(Position position) {

    }

    @Override
    public void onFill(Order filled) {

    }

    @Override
    public void onPositionHistory(Position historied) {

    }

    @Override
    public void onPlaceOrder(Order place) {

    }

    @Override
    public void onCancelOrder(Order cancel) {

    }

    @Override
    public void onOrderAlgo(Order pushed) {

    }

    @Override
    public void onLogined(WSResult result) {

    }

    @Override
    public void onSnaphot(String json) {

    }

    @Override
    public void onUpdate(String json) {

    }

    @Override
    public void onError(WSResult result) {

    }

    @Override
    public void onSubscride(SubscribeResult result) {

    }

    @Override
    public void onUnsubscribe(SubscribeResult result) {

    }

    @Override
    public void onDefault(String json) {

    }
}
