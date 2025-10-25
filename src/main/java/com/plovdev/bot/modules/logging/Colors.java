package com.plovdev.bot.modules.logging;

public enum Colors {
    Green("\u001B[32m"), Yellow("\u001B[33m"), Red("\u001B[31m"),
    Blue("\u001B[34m"), Black("\u001B[30m"), Purpure("\u001B[35m"), Blue2("\u001B[36m"), White("\u001B[37m"), Bold("\u001B[1m"), Underline("\u001B[4m"), Clear("\u001B[0m");

    private final String color;
    Colors(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return this.color;
    }
}