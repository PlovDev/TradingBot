package com.plovdev.bot.modules.models;

import java.math.BigDecimal;

public class StopInProfitTrigger {
    private String variant;
    private int takeToTrailNumber = 0;

    // Куда переносить стоп
    private BigDecimal stopInProfitPercent = new BigDecimal("0.0");

    //Только для x profit
    private BigDecimal profitPercent = new BigDecimal("0.0");
    private static final SettingsService service = new SettingsService();

    public StopInProfitTrigger() {
    }

    public static StopInProfitTrigger load(String group) {
        String variant = service.getStopInProfitVariant(group);
        String[] strs = service.getStopInProfitByGroup(group).split(",");
        int num = 0;
        BigDecimal per = new BigDecimal("0.0");
        if (variant.equals("take")) {
            num = Integer.parseInt(strs[0].replace(",", ""));
        } else {
            per = new BigDecimal(strs[0].replace(",", ""));
        }
        return new StopInProfitTrigger(variant, num, new BigDecimal(strs[1].replace(",", "")), per);
    }

    public StopInProfitTrigger(String variant, int takeToTrailNumber, BigDecimal stopInProfitPercent, BigDecimal profitPercent) {
        this.variant = variant;
        this.takeToTrailNumber = takeToTrailNumber;
        this.stopInProfitPercent = stopInProfitPercent;
        this.profitPercent = profitPercent;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public int getTakeToTrailNumber() {
        return Math.max(takeToTrailNumber-1, 0);
    }

    public void setTakeToTrailNumber(int takeToTrailNumber) {
        this.takeToTrailNumber = takeToTrailNumber;
    }

    public BigDecimal getStopInProfitPercent() {
        return stopInProfitPercent;
    }

    public void setStopInProfitPercent(BigDecimal stopInProfitPercent) {
        this.stopInProfitPercent = stopInProfitPercent;
    }

    public BigDecimal getProfitPercent() {
        return profitPercent;
    }

    public void setProfitPercent(BigDecimal profitPercent) {
        this.profitPercent = profitPercent;
    }

    public boolean isTakeVariant() {
        return "take".equals(variant);
    }
    public boolean isProfitVariant() {
        return "profit".equals(variant);
    }


    @Override
    public String toString() {
        return "StopInProfitTrigger{" +
                "variant='" + variant + '\'' +
                ", takeToTrailNumber=" + takeToTrailNumber +
                ", stopInProfitPercent=" + stopInProfitPercent +
                ", profitPercent=" + profitPercent +
                '}';
    }
}