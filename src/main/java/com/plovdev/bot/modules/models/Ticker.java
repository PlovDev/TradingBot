package com.plovdev.bot.modules.models;

import java.math.BigDecimal;
import java.util.Objects;

public class Ticker {
    private String symbol;
    private BigDecimal lastPrice;
    private BigDecimal markPrice;

    @Override
    public String toString() {
        return "Ticker{" +
                "symbol='" + symbol + '\'' +
                ", lastPrice=" + lastPrice +
                ", markPrice=" + markPrice +
                '}';
    }

    public Ticker(String symbol, BigDecimal lastPrice, BigDecimal markPrice) {
        this.symbol = symbol;
        this.lastPrice = lastPrice;
        this.markPrice = markPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public BigDecimal getMarkPrice() {
        return markPrice;
    }

    public void setMarkPrice(BigDecimal markPrice) {
        this.markPrice = markPrice;
    }

    public Ticker() {

    }

    public Ticker(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ticker ticker = (Ticker) o;
        return Objects.equals(symbol, ticker.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }
}