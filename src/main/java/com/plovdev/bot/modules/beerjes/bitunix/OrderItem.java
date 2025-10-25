package com.plovdev.bot.modules.beerjes.bitunix;

import java.math.BigDecimal;

public class OrderItem {
    private String orderId;
    private BigDecimal price;
    private BigDecimal qty;
    private String type;
    private String side;
    private String orderStatus;
    private int leverage;
    private String positionMode;
    private boolean reduceOnly;

    public OrderItem() {
    }

    public OrderItem(String orderId, String price, String qty, String type, String side, String orderStatus, int leverage, String positionMode, boolean reduceOnly) {
        this.orderId = orderId;
        this.price = new BigDecimal(price);
        this.qty = new BigDecimal(qty);
        this.type = type;
        this.side = side;
        this.orderStatus = orderStatus;

        this.leverage = leverage;
        this.positionMode = positionMode;
        this.reduceOnly = reduceOnly;
    }


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = new BigDecimal(price);
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = new BigDecimal(qty);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public int getLeverage() {
        return leverage;
    }

    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    public String getPositionMode() {
        return positionMode;
    }

    public void setPositionMode(String positionMode) {
        this.positionMode = positionMode;
    }

    public boolean isReduceOnly() {
        return reduceOnly;
    }

    public void setReduceOnly(boolean reduceOnly) {
        this.reduceOnly = reduceOnly;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderId='" + orderId + '\'' +
                ", price='" + price + '\'' +
                ", qty='" + qty + '\'' +
                ", type='" + type + '\'' +
                ", side='" + side + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", leverage=" + leverage +
                ", positionMode='" + positionMode + '\'' +
                ", reduceOnly=" + reduceOnly +
                '}';
    }
}