package com.plovdev.bot.modules.models;

public class PositionsModel {
    private String id;
    private String pair;

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    private String direction;
    private String enter;
    private String plecho;
    private String pnl;
    private String open;
    private String close;
    private String state;
    private String total;

    public PositionsModel() {

    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getPnl() {
        return pnl;
    }

    public void setPnl(String pnl) {
        this.pnl = pnl;
    }

    public String getPlecho() {
        return plecho;
    }

    public void setPlecho(String plecho) {
        this.plecho = plecho;
    }

    public String getEnter() {
        return enter;
    }

    public void setEnter(String enter) {
        this.enter = enter;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "PositionsModel{" +
                "id='" + id + '\'' +
                ", pair='" + pair + '\'' +
                ", direction='" + direction + '\'' +
                ", enter='" + enter + '\'' +
                ", plecho='" + plecho + '\'' +
                ", pnl='" + pnl + '\'' +
                ", open='" + open + '\'' +
                ", close='" + close + '\'' +
                ", state='" + state + '\'' +
                ", total='" + total + '\'' +
                '}';
    }

    public PositionsModel(String ... position) {
        id = position[0];
        pair = position[1];
        direction = position[2];
        enter = position[3];
        plecho = position[4];
        pnl = position[5];
        open = position[6];
        close = position[7];
        state = position[8];
        total = position[9];
    }
}