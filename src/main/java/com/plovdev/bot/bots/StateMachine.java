package com.plovdev.bot.bots;

import com.plovdev.bot.modules.databases.UserDB;
import com.plovdev.bot.modules.databases.base.Entity;
import org.slf4j.*;
import org.telegram.telegrambots.meta.api.objects.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Класс, управляющий состояниями пользователя.
 * Каждое состояние - свое имя и то, что должно выполниться.
 */
public class StateMachine {
    private final List<State> states = new CopyOnWriteArrayList<>(); // Список состояний
    private final Logger logger = LoggerFactory.getLogger("State Machine"); // Логгер


    /**
     * Базовый конструктор
     */
    public StateMachine() {

    }
    /**
     * Конструкор, который сразу добавляет состояния в список.
     * @param stts - состояния, для добавления.
     */
    public StateMachine(State ... stts) {
        states.addAll(Arrays.stream(stts).toList());
    }

    /**
     * Вторая вариация конструктора с добавлением состояний.
     * @param states - список состояний на добавление.
     */
    public StateMachine(List<State> states) {
        this.states.addAll(states);
    }

    /**
     * Метод проходится по сотояниям, и выполняет то состояние, которое указанно в newState.
     * @param newState - Текущее состояние пользователя.
     * @param update - телеграм обновление.
     */
    public void notifyStates(String newState, Update update) {
        String comand = update.getMessage().getText(); // команда, пришедшая с телеграма.
        Message message = update.getMessage(); // Сообщение, пришедшее с тг, нужно для удобства:)

        User from = message.getFrom(); // Тот, от кого пришло сообщение или команда.
        String chatId = message.getChatId().toString(); // ID чата, в котором пришло сообщение для бота.

        Entity repository = new UserDB().get(from.getId().toString()); // репозиторий с пользователем
        // Нужен для передачи в параметры.

        // Цикл по всем добавленным состояниям
        for (State state : states) {
            // Проверяем, что имя состояния равно тому, то указано в newState.
            if (newState.startsWith(state.name())) {
                try {
                    // Выполняем работу состояния, и передаем параметры в метод.
                    state.toDo().onState(update, message, from, chatId, comand, repository, newState);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Произошла ошибка выполнения состояния: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Добавить состояние в список.
     * @param state - состояние.
     */
    public void addState(State state) {
        states.add(state);
    }

    /**
     * Удалить состояние из списка.
     * @param state - состояние.
     */
    public void removeState(State state) {
        states.remove(state);
    }

    /**
     * Добавить несколько состояний в список с помощью аргументов переменной длины.
     * @param states - состояния.
     */
    public void addAllStates(State ... states) {
        addAll(Arrays.stream(states).toList());
    }

    /**
     * Добавить несколько состояний списком.
     * @param states - состояния.
     */
    public void addAllState(List<State> states) {
        addAll(states);
    }

    /**
     * Удалить несколько состояний из списка с помощью аргументов переменной длины.
     * @param states - состояния на удаление.
     */
    public void removeAllStates(State ... states) {
        removeAll(Arrays.stream(states).toList());
    }

    /**
     * Удалить несколько состояний списком.
     * @param states - состояния на удаление.
     */
    public void removeAllStates(List<State> states) {
        removeAll(states);
    }


    /**
     * Базовый метод множественного добавления.
     * @param stts - состояния на добавление, списком.
     */
    private void addAll(List<State> stts) {
        states.addAll(stts);
    }

    /**
     * Базовый метод множественного удаления.
     * @param stts - состояния на удаление, списком.
     */
    private void removeAll(List<State> stts) {
        states.removeAll(stts);
    }
}