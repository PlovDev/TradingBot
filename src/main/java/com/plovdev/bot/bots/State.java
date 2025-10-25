package com.plovdev.bot.bots;


import org.jetbrains.annotations.NotNull;

/**
 * Состояние пользователя.
 * @param name - название состояние.
 * @param toDo Что сделать, при выполнении состояния.
 */
public record State(String name, StateListener toDo) {

    /**
     * Переопределяет метод toString.
     * @return - возвращает строковое прдставление об объекте.
     */
    @NotNull
    @Override
    public String toString() {
        return "[" + name + "]";
    }
}