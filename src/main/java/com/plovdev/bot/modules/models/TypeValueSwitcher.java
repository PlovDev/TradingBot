package com.plovdev.bot.modules.models;

public class TypeValueSwitcher<T> {
    private T t;

    public TypeValueSwitcher(T t) {
        this.t = t;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return t.toString();
    }
}