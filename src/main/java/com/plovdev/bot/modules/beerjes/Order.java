package com.plovdev.bot.modules.beerjes;

import java.math.BigDecimal;

public class Order {
    private String symbol;
    private BigDecimal size;
    private String orderId;
    private String client0Id;
    private BigDecimal filledQty;
    private String fee;
    private BigDecimal price;
    private boolean isStopTraling = false;

    public boolean isStopTraling() {
        return isStopTraling;
    }

    public void setStopTraling(boolean stopTraling) {
        isStopTraling = stopTraling;
    }

    @Override
    public String toString() {
        return "Order{" +
                "symbol='" + symbol + '\'' +
                ", size=" + size +
                ", orderId='" + orderId + '\'' +
                ", client0Id='" + client0Id + '\'' +
                ", filledQty=" + filledQty +
                ", fee='" + fee + '\'' +
                ", price=" + price +
                ", state='" + state + '\'' +
                ", side='" + side + '\'' +
                ", timeInForce='" + timeInForce + '\'' +
                ", totalProfits='" + totalProfits + '\'' +
                ", posSide='" + posSide + '\'' +
                ", merginCoin='" + merginCoin + '\'' +
                ", takeProfitPrice=" + takeProfitPrice +
                ", stopLossPrice=" + stopLossPrice +
                ", filledAmount=" + filledAmount +
                ", orderType='" + orderType + '\'' +
                ", leverage=" + leverage +
                ", marginMode='" + marginMode + '\'' +
                ", reduceOnly=" + reduceOnly +
                ", tradeSide='" + tradeSide + '\'' +
                ", holdMode='" + holdMode + '\'' +
                ", orderSource='" + orderSource + '\'' +
                ", cTime='" + cTime + '\'' +
                ", uTime='" + uTime + '\'' +
                '}';
    }

    private String state;
    private String side;
    private String timeInForce;
    private String totalProfits;
    private String posSide;
    private String merginCoin;
    private BigDecimal takeProfitPrice;
    private BigDecimal stopLossPrice;
    private BigDecimal filledAmount;
    private String orderType;
    private int leverage;
    private String marginMode;
    private boolean reduceOnly;
    private String tradeSide;
    private String holdMode;
    private String orderSource;
    private String cTime;
    private String uTime;

    public Order() {}

    public Order(String symbol, BigDecimal size, String orderId, String client0Id, BigDecimal filledQty, String fee, BigDecimal price, String state, String side, String timeInForce, String totalProfits, String posSide, String merginCoin, BigDecimal takeProfitPrice, BigDecimal stopLossPrice, BigDecimal filledAmount, String orderType, int leverage, String marginMode, boolean reduceOnly, String tradeSide, String holdMode, String orderSource, String cTime, String uTime) {
        this.symbol = symbol;
        this.size = size;
        this.orderId = orderId;
        this.client0Id = client0Id;
        this.filledQty = filledQty;
        this.fee = fee;
        this.price = price;
        this.state = state;
        this.side = side;
        this.timeInForce = timeInForce;
        this.totalProfits = totalProfits;
        this.posSide = posSide;
        this.merginCoin = merginCoin;
        this.takeProfitPrice = takeProfitPrice;
        this.stopLossPrice = stopLossPrice;
        this.filledAmount = filledAmount;
        this.orderType = orderType;
        this.leverage = leverage;
        this.marginMode = marginMode;
        this.reduceOnly = reduceOnly;
        this.tradeSide = tradeSide;
        this.holdMode = holdMode;
        this.orderSource = orderSource;
        this.cTime = cTime;
        this.uTime = uTime;
    }

    public String getuTime() {
        return uTime;
    }

    public void setuTime(String uTime) {
        this.uTime = uTime;
    }

    public String getcTime() {
        return cTime;
    }

    public void setcTime(String cTime) {
        this.cTime = cTime;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public String getHoldMode() {
        return holdMode;
    }

    public void setHoldMode(String holdMode) {
        this.holdMode = holdMode;
    }

    public String getTradeSide() {
        return tradeSide;
    }

    public void setTradeSide(String tradeSide) {
        this.tradeSide = tradeSide;
    }

    public boolean isReduceOnly() {
        return reduceOnly;
    }

    public void setReduceOnly(boolean reduceOnly) {
        this.reduceOnly = reduceOnly;
    }

    public String getMarginMode() {
        return marginMode;
    }

    public void setMarginMode(String marginMode) {
        this.marginMode = marginMode;
    }

    public int getLeverage() {
        return leverage;
    }

    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public BigDecimal getFilledAmount() {
        return filledAmount;
    }

    public void setFilledAmount(BigDecimal filledAmount) {
        this.filledAmount = filledAmount;
    }

    public BigDecimal getStopLossPrice() {
        return stopLossPrice;
    }

    public void setStopLossPrice(BigDecimal stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    public BigDecimal getTakeProfitPrice() {
        return takeProfitPrice;
    }

    public void setTakeProfitPrice(BigDecimal takeProfitPrice) {
        this.takeProfitPrice = takeProfitPrice;
    }

    public String getMerginCoin() {
        return merginCoin;
    }

    public void setMerginCoin(String merginCoin) {
        this.merginCoin = merginCoin;
    }

    public String getPosSide() {
        return posSide;
    }

    public void setPosSide(String posSide) {
        this.posSide = posSide;
    }

    public String getTotalProfits() {
        return totalProfits;
    }

    public void setTotalProfits(String totalProfits) {
        this.totalProfits = totalProfits;
    }

    public String getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public BigDecimal getFilledQty() {
        return filledQty;
    }

    public void setFilledQty(BigDecimal filledQty) {
        this.filledQty = filledQty;
    }

    public String getClient0Id() {
        return client0Id;
    }

    public void setClient0Id(String client0Id) {
        this.client0Id = client0Id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getSize() {
        return size;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}