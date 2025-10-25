package com.plovdev.bot.bots;

// Импортируем зависимости))
import com.plovdev.bot.modules.databases.UserDB;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.databases.base.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Утилитный класс, который обеспечивает работы с командами бота, добавить/удалить/уведомить.
 */
public class CommandHandler {
    private static final ConcurrentHashMap<List<String>, ComandListener> comands = new ConcurrentHashMap<>(); // Словарь слушателей. Comand == ComandListener
    private static final Logger logger = LoggerFactory.getLogger("CommandHandler"); // Логгер


    /**
     * Метод, который регистрирует нового слушателя на команду.
     * @param command - команда, через которую произойдет срабатывание слушателя.
     * @param listener - сам слушатель;
     */
    public static void registerComand(String command, ComandListener listener, String ... strs) {
        List<String> strings = new ArrayList<>(List.of(command));
        strings.addAll(Arrays.stream(strs).toList());
        comands.put(strings, listener);
    }

    /**
     * Удаляет слушателя.
     * @param comand - команда, которую надоперестать слушать.
     */
    public static void removeComand(List<String> comand) {
        comands.remove(comand);
    }

    // Глобальный фильтр, предткат выполнения команд.
    private static GlobalFilter filter = (s) -> true;


    /**
     * notifyComands - метод, который выполняет пришедшие с телеграма команды.
     * @param update - Обновление с телеграма. Нужно для педедачи в параметры другим слушателям.
     * @return - возвращает булево значение.
     * True - если была выполнена хоть одна команда, и false если ничего не выполено.
     */
    public static boolean notifyComands(Update update) {
        int count = 0; // счетчик выполненных команд.
        String comand = update.getMessage().getText(); // команда, пришедшая с телеграма.
        Message message = update.getMessage(); // Сообщение, пришедшее с тг, нужно для удобства:)

        User from = message.getFrom(); // Тот, от кого пришло сообщение или команда.
        String chatId = from.getId().toString(); // ID чата, в котором пришло сообщение для бота.

        Entity repository = new UserDB().get(from.getId().toString()); // репозиторий с пользователем
        UserEntity entity = (UserEntity) repository;
        System.out.println(entity.getExpiry() + " -expr, id: " + entity.getTgId());
        // Нужен для передачи в параметры.

        /*
        Цикл для прохода по всем слушателям команд.

        CMD - зарегестрированная команда, то, на что подписывались слушатели.
         */
        for (List<String> cmds : comands.keySet()) {
            try {
                for (String cmd : cmds) {
                    if (comand.startsWith(cmd)) { // Если comand(пришедшая команда)
                        // начинаеться с тем, на что подписывались слушатели, то выполняем условие.
                        // Пример: /start - input, /start - listen.
                        // Пример2: /strt - input, /start - listen.

                        boolean f = filter.execute(message);
                        System.out.println(f);
                        if (f) { // проверяем глобальный фильтр выполнения команд.
                            // получаем слушателя по названию команды, и выполняем кго.
                            comands.get(cmds).onCommand(update, message, from, chatId, comand, repository);
                            count++; // увеличиваем счетчик выполненных команд.
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Произошла ошибка обработки и выполнения команд: {}", e.getMessage());
            }
        }

        return !(count == 0);
    }

    public static void setGlobalFilter(GlobalFilter globalFilter) {
        filter = globalFilter;
    }

    /**
     * Метод для уведомления слушателей нажатия кнопок о том, что пользователь нажал на кнопку в тг7.
     * @param update - Обновление, пришедшее с ТГ.
     * @return - возвращает true если хоть 1 колбек был сработан, или false если ничего не выполнилось.
     */
    public static boolean notifyButtons(Update update) {
        int count = 0; // Счетчик выполненых калбеков.
        String callback = update.getCallbackQuery().getData(); // Сам текст калбека.

        CallbackQuery query = update.getCallbackQuery(); // Сам калбек.
        Message message = query.getMessage(); // Сообщение, пришедшее вместе с калбеком.

        User from = query.getFrom(); // Кто нажал на кнопку.
        String chatId = from.getId().toString(); // ID чата, где пользователь нажал на кнопку.

        Entity repository = new UserDB().get(from.getId().toString()); // репозиторий с пользователем
        // Нужен для передачи в параметры.


        /*
        Цикл для прохода по всем слушателям команд.

        CMD - зарегестрированный калбек, то, на что подписывались слушатели.
         */
        for (List<String> cmds : comands.keySet()) {
            try {
                for (String cmd : cmds) {
                    if (callback.startsWith(cmd)) {
                        // Если callbak(пришедший калбек)
                        // начинаеться с тем, на что подписывались слушатели, то выполняем условие.
                        // Пример: ACCEPT_BUTTON - input, ACCEPT_BUTTON - listen.
                        // Пример2: ACCPT_BUTTON - input, ACCEPT_BUTTON - listen.

                        if (filter.execute(message)) { // проверяем глобальный фильтр.
                            comands.get(cmds).onCommand(update, message, from, chatId, callback, repository); // получаем слушателя, и выполняем его.
                            count++; // увеличиваем счетчик.
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Произошла ошибка обработки и выполнения кнопок: {}", e.getMessage());
            }
        }

        return !(count == 0);
    }
}