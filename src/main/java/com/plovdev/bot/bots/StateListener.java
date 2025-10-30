package com.plovdev.bot.bots;


import com.plovdev.bot.modules.databases.base.Entity;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

/**
 * Функциональный интерфейс, содержащий метод, который выполниться при приходе
 * калбека кнопки, или сообщения в ТГ, если таковые зарегестрированны.
 */

@FunctionalInterface
public interface StateListener {
    /**
     * Метод, который выполниться при срабатывании события, такого как нажатие на кнопку
     * или ввода команды боту.
     * @param update - Базвый Update для того, чтобы слушатели смогли сделать все что им надо.
     * @param from - от кого пришло сообщение или калбек. Передаеться в параметры для удобства.
     * @param chatId - ID чата, в котором произошло событие.
     * @param text - текст сообщения. То есть команды ИЛИ пришедшего калбека.
     * @param repository - Entity, то кого пришло сообщение. Упращает работу с
     *                   базой данных.
     *
     * @throws Exception - любое исклбчение, кинутое внутри метода. Обрабатываеться в CommandListener.
     */
    void onState(Update update, Message message, User from, String chatId, String text, Entity repository, String stateName) throws Exception;
}
