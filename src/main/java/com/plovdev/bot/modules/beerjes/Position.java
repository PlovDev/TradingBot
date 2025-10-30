package com.plovdev.bot.modules.beerjes;

import java.math.BigDecimal;

public class Position {
    private String posId;
    private boolean isStopTraling;

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    private BigDecimal entryPrice;

    public boolean isStopTraling() {
        return isStopTraling;
    }

    public void setStopTraling(boolean stopTraling) {
        isStopTraling = stopTraling;
    }

    public Position() {
    }

    public Position(String posId, String marginCoin, String symbol, String holdSide, BigDecimal openDelegateCount, BigDecimal available, BigDecimal locked, BigDecimal total, int leverage, BigDecimal unrealizedPL, BigDecimal liquidationPrice, BigDecimal keepMarginRate, BigDecimal marketPrice, String cTime, String uTime, BigDecimal openAvgPrice, boolean st) {
        this.posId = posId;
        this.marginCoin = marginCoin;
        this.symbol = symbol;
        this.holdSide = holdSide;
        this.openDelegateCount = openDelegateCount;
        this.available = available;
        this.locked = locked;
        this.total = total;
        this.leverage = leverage;
        this.unrealizedPL = unrealizedPL;
        this.liquidationPrice = liquidationPrice;
        this.keepMarginRate = keepMarginRate;
        this.marketPrice = marketPrice;
        this.cTime = cTime;
        this.uTime = uTime;
        isStopTraling = st;
        entryPrice = openAvgPrice;
    }

    @Override
    public String toString() {
        return "Position{" +
                "posId='" + posId + '\'' +
                ", marginCoin='" + marginCoin + '\'' +
                ", symbol='" + symbol + '\'' +
                ", holdSide='" + holdSide + '\'' +
                ", openDelegateCount=" + openDelegateCount +
                ", margin=" + margin +
                ", autoMargin='" + autoMargin + '\'' +
                ", available=" + available +
                ", locked=" + locked +
                ", total=" + total +
                ", leverage=" + leverage +
                ", unrealizedPL=" + unrealizedPL +
                ", liquidationPrice=" + liquidationPrice +
                ", keepMarginRate=" + keepMarginRate +
                ", marketPrice=" + marketPrice +
                ", cTime='" + cTime + '\'' +
                ", uTime='" + uTime + '\'' +
                '}';
    }

    public String getPosId() {
        return posId;
    }

    public void setPosId(String posId) {
        this.posId = posId;
    }

    private String marginCoin;
    private String symbol;
    private String holdSide;
    private BigDecimal openDelegateCount;
    private BigDecimal margin;
    private String autoMargin;
    private BigDecimal available;
    private BigDecimal locked;
    private BigDecimal total;
    private int leverage;
    private BigDecimal unrealizedPL;
    private BigDecimal liquidationPrice;
    private BigDecimal keepMarginRate;
    private BigDecimal marketPrice;
    private String cTime;
    private String uTime;


    public String getMarginCoin() {
        return marginCoin;
    }

    public void setMarginCoin(String marginCoin) {
        this.marginCoin = marginCoin;
    }

    public String getHoldSide() {
        return holdSide;
    }

    public void setHoldSide(String holdSide) {
        this.holdSide = holdSide;
    }

    public BigDecimal getOpenDelegateCount() {
        return openDelegateCount;
    }

    public void setOpenDelegateCount(BigDecimal openDelegateCount) {
        this.openDelegateCount = openDelegateCount;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public String getAutoMargin() {
        return autoMargin;
    }

    public void setAutoMargin(String autoMargin) {
        this.autoMargin = autoMargin;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public BigDecimal getLocked() {
        return locked;
    }

    public void setLocked(BigDecimal locked) {
        this.locked = locked;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public int getLeverage() {
        return leverage;
    }

    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    public BigDecimal getUnrealizedPL() {
        return unrealizedPL;
    }

    public void setUnrealizedPL(BigDecimal unrealizedPL) {
        this.unrealizedPL = unrealizedPL;
    }

    public BigDecimal getLiquidationPrice() {
        return liquidationPrice;
    }

    public void setLiquidationPrice(BigDecimal liquidationPrice) {
        this.liquidationPrice = liquidationPrice;
    }

    public BigDecimal getKeepMarginRate() {
        return keepMarginRate;
    }

    public void setKeepMarginRate(BigDecimal keepMarginRate) {
        this.keepMarginRate = keepMarginRate;
    }

    public BigDecimal getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(BigDecimal marketPrice) {
        this.marketPrice = marketPrice;
    }

    public String getcTime() {
        return cTime;
    }

    public void setcTime(String cTime) {
        this.cTime = cTime;
    }

    public String getuTime() {
        return uTime;
    }

    public void setuTime(String uTime) {
        this.uTime = uTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
