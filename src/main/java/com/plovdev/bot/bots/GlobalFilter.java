package com.plovdev.bot.bots;

import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Предикат выполнения и обработки команд или калбеков.
 */
public interface GlobalFilter {
    /**
     * Тут ставиться условие выпонения команды или калбека.
     * @param message - пришедшее сообщение(команда или калбек)
     * @return - возвращает true если команда или калбек будет обработан(а),
     * false - если не будет обрабатываться.
     */
    boolean execute(Message message);
}
