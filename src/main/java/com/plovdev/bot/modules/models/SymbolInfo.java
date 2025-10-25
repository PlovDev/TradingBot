package com.plovdev.bot.modules.models;

import java.math.BigDecimal;

public class SymbolInfo {
    private String symbol;
    private String baseCoin;
    private int pricePlace;
    private int volumePlace;
    private BigDecimal minTradeNum;
    private BigDecimal sizeMultiplier;

    public BigDecimal getSizeMultiplier() {
        return sizeMultiplier;
    }

    public void setSizeMultiplier(BigDecimal sizeMultiplier) {
        this.sizeMultiplier = sizeMultiplier;
    }

    public SymbolInfo(String symbol, String baseCoin, int pricePlace, int volumePlace, BigDecimal minTradeNum, int maxLever, BigDecimal sm) {
        this.symbol = symbol;
        this.baseCoin = baseCoin;
        this.pricePlace = pricePlace;
        this.volumePlace = volumePlace;
        this.minTradeNum = minTradeNum;
        this.maxLever = maxLever;
        sizeMultiplier = sm;
    }

    public BigDecimal getMinTradeNum() {
        return minTradeNum;
    }

    public void setMinTradeNum(BigDecimal minTradeNum) {
        this.minTradeNum = minTradeNum;
    }

    public int getVolumePlace() {
        return volumePlace;
    }

    public void setVolumePlace(int volumePlace) {
        this.volumePlace = volumePlace;
    }

    private int maxLever;

    @Override
    public String toString() {
        return "SymbolInfo{" +
                "symbol='" + symbol + '\'' +
                ", baseCoin='" + baseCoin + '\'' +
                ", pricePlace=" + pricePlace +
                ", maxLever=" + maxLever +
                '}';
    }

    public SymbolInfo() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBaseCoin() {
        return baseCoin;
    }

    public void setBaseCoin(String baseCoin) {
        this.baseCoin = baseCoin;
    }

    public int getPricePlace() {
        return pricePlace;
    }

    public void setPricePlace(int pricePlace) {
        this.pricePlace = pricePlace;
    }

    public int getMaxLever() {
        return maxLever;
    }

    public void setMaxLever(int maxLever) {
        this.maxLever = maxLever;
    }
}