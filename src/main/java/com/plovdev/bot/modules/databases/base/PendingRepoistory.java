package com.plovdev.bot.modules.databases.base;

public class PendingRepoistory implements Entity {
    private String id;
    private String uid;
    private String username;
    private String state;
    private String type;
    private String beerj;

    public String getBeerj() {
        return beerj;
    }

    public void setBeerj(String beerj) {
        this.beerj = beerj;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PendingRepoistory() {

    }
    public PendingRepoistory(String ... strings) {
        id = strings[0];
        uid = strings[1];
        username = strings[2];
        state = strings[3];
        type = strings[4];
        beerj = strings[5];
    }
}