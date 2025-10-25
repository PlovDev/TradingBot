package com.plovdev.bot.bots;

import com.plovdev.bot.modules.databases.TemplateDB;
import com.plovdev.bot.modules.databases.UserDB;
import com.plovdev.bot.modules.databases.UserEntity;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.concurrent.ConcurrentHashMap;

public class TemplatesHandler {
    private static final UserDB adminsDB = new UserDB();
    private static final TemplateDB templatesDB = new TemplateDB();
    private static final ConcurrentHashMap<String, String> names = new ConcurrentHashMap<>();

    public static void handle(Message input, TelegramLongPollingBot bot) throws Exception {
        User user = input.getFrom();
        String userId = user.getId().toString();
        UserEntity repository = (UserEntity) adminsDB.get(userId);
        String state = repository.getState();


        if (!state.equals("none") && input.getText().startsWith("/")) {
            if (input.getText().startsWith("/cancel")) {
                adminsDB.update("state", "none", userId);
                bot.execute(new SendMessage(userId, "Отменено"));
            }
            return;
        }

        if (state.startsWith("gettingTemplNName:")) {

            String tmp = state.substring(state.lastIndexOf(':')+1);
            templatesDB.upadateByKey("name", input.getText(), tmp);
            adminsDB.update("state", "none", userId);
            bot.execute(new SendMessage(userId, "Название измененно!"));

        } else if (state.startsWith("gettingTemplNRu:")) {

            String tmp = state.substring(state.lastIndexOf(':')+1);
            templatesDB.upadateByKey("ru", input.getText(), tmp);
            adminsDB.update("state", "none", userId);
            bot.execute(new SendMessage(userId, "Русский текст изменен!"));

        } else if (state.startsWith("gettingTemplNEn:")) {

            String tmp = state.substring(state.lastIndexOf(':')+1);
            templatesDB.upadateByKey("en", input.getText(), tmp);
            adminsDB.update("state", "none", userId);
            bot.execute(new SendMessage(userId, "Английский текст изменен!"));

        } else if (state.startsWith("gettingTemplNMd:")) {

            String tmp = state.substring(state.lastIndexOf(':')+1);
            templatesDB.upadateByKey("md", input.getText(), tmp);
            adminsDB.update("state", "none", userId);
            bot.execute(new SendMessage(userId, "Молдавский текст изменен!"));

        }

        switch (state) {
            case "none" -> {
                adminsDB.update("state", "gettingTemplName", userId);
                SendMessage message = new SendMessage(userId, "Введите название шаблона:");
                bot.execute(message);
            }
            case "gettingTemplName" -> {
                names.put(userId, input.getText().trim());
                adminsDB.update("state", "gettingTemplTextRu", userId);

                bot.execute(new SendMessage(userId, "Введите русский текст:"));
            }
            case "gettingTemplTextRu" -> {
                templatesDB.add(names.get(userId), "", input.getText(), "");
                adminsDB.update("state", "gettingTemplTextEn", userId);

                bot.execute(new SendMessage(userId, "Введите английский текст:"));
            }
            case "gettingTemplTextEn" -> {
                templatesDB.upadateByKey("en", input.getText(), names.get(userId));
                adminsDB.update("state", "gettingTemplTextMd", userId);

                bot.execute(new SendMessage(userId, "Введите молдавский текст:"));
            }
            case "gettingTemplTextMd" -> {
                templatesDB.upadateByKey("md", input.getText(), names.get(userId));
                adminsDB.update("state", "none", userId);

                names.remove(userId);
                bot.execute(new SendMessage(userId, "Шаблон добавлен!"));
            }
        }
    }
}