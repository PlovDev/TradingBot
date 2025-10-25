package com.plovdev.bot.modules.beerjes;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class TakeProfitLevel implements Comparable<BigDecimal> {
    private BigDecimal size;
    private BigDecimal price;

    public TakeProfitLevel(BigDecimal size, BigDecimal price) {
        this.size = size;
        this.price = price;
    }

    public TakeProfitLevel(String size, String price) {
        this.size = new BigDecimal(size);
        this.price = new BigDecimal(price);
    }
    public TakeProfitLevel(BigDecimal size) {
        this.size = size;
    }
    public TakeProfitLevel(String size) {
        this.size = new BigDecimal(size);
    }
    public TakeProfitLevel() {}

    // геттеры и сеттеры
    public BigDecimal getSize() { return size; }
    public void setSize(BigDecimal size) { this.size = size; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }


    @Override
    public String toString() {
        return String.format("SIZE: %s", size.toPlainString()) + (price == null? "" : " PRICE: " + price.toPlainString());
    }

    @Override
    public int compareTo(@NotNull BigDecimal o) {
        return price.compareTo(o);
    }
}