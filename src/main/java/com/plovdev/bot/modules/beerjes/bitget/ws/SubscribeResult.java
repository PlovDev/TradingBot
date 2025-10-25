package com.plovdev.bot.modules.beerjes.bitget.ws;

public class SubscribeResult {
    private String symbol;
    private Channel channel;
    private Type type;
    private boolean isSuccess;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public SubscribeResult() {
    }

    public SubscribeResult(String symbol, Channel channel, Type type, boolean suc) {
        this.symbol = symbol;
        this.channel = channel;
        this.type = type;
        isSuccess = suc;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Channel getChanel() {
        return channel;
    }

    public void setChanel(Channel channel) {
        this.channel = channel;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("Symbol: %s, Type: %s, Channel: %s", symbol, type.getType(), channel.getName());
    }
}