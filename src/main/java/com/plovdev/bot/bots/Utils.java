package com.plovdev.bot.bots;

import com.plovdev.bot.modules.databases.UserDB;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.exceptions.ApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.prefs.Preferences;

public class Utils {

    private static final LanguageManager manager = new LanguageManager();
    /**
     * Самописный метод, который проверяет, что строка содержит ТОЛЬКО числа.
     * @param input - строка на проверку.
     * @return true - если строка являеться числом, false - если нет.
     */
    public static boolean isOnlyNumbers(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void sendNoPerms(TelegramLongPollingBot bot, UserEntity entity) {
        try {
            bot.execute(new SendMessage(entity.getTgId(), manager.getText(entity.getLanguage(), "noPerms")));
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void sendUnreg(TelegramLongPollingBot bot, UserEntity entity, String chat) {
        try {
            String lang = entity.getLanguage();

            String text = lang == null? "\uD83D\uDD14 You haven't completed profile setup. Please enter the missing information." : manager.getText(lang, "regFailed");
            bot.execute(new SendMessage(chat, text));
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Метод, который разбивает ввод на слова, по шаблону:
     * NAME: TEXT,
     * @param text - строка для парсинга.
     * @return - возвращает список разбитых слов. В данном случае вернет TEXT
     */
    public static List<String> parseInput(String text) {
        text = text.replace(" ", "");

        String[] splits = text.split("\n");
        List<String> strings = new ArrayList<>();

        for (String line : splits) {
            String s = line.substring(line.indexOf(':')+1);
            strings.add(s);
        }

        return strings;
    }

    public static boolean cancelLimits(String pait) {
        System.out.println("Canceled");
        return false;
    }

    /**
     * Метод, который возвращает токен бота.
     * @return - токен бота.
     */
    public static String getBotToken() {
        return Preferences.userRoot().node("TradingBot").get("token", "8284316491:AAFYtF4qp1kamHVby1z1KOGEnGmdWnK9xXU");
    }

    /**
     * Заменяет null на строку-аналог.
     * @param nullStr - строка, которая может быть null.
     * @param replacement - на что заменить null-строку.
     * @return возвращает замененный null, на строку.
     */
    public static String replaceNull(String nullStr, String replacement) {
        return nullStr == null? replacement: nullStr;
    }


    /**
     * Сохраняет репозиторий пользователя в базу.
     * @param from - кого сохранить.
     * @param lang - язык пользователя.
     * @param userDB - база данных для сохранения.
     */
    public static void saveRepo(User from, String lang, UserDB userDB, String variant) throws ApiException {
        UserEntity repository = new UserEntity();

        repository.setTgName("@" + from.getUserName());
        repository.setFirstName(from.getFirstName());
        repository.setLastName(from.getLastName());
        repository.setTgId(from.getId().toString());
        repository.setUID("none");
        repository.setWC("0");
        repository.setReferral("none");
        repository.setLanguage(lang);
        repository.setState("none");
        repository.setRole("user");
        repository.setApiKey("none");
        repository.setSecretKey("none");
        repository.setPhrase("none");

        repository.setName(from.getUserName());
        repository.setPositions("none");
        repository.setPlecho("none");
        repository.setVariant("none");
        repository.setSum("none");
        repository.setProc("none");
        repository.setBeerj("none");
        repository.setExpiry("unlimited");
        repository.setStatus("REGISTRATION");
        repository.setRegVariant(variant);
        repository.setGroup("common");
        repository.setGetRew(false);
        repository.setActiveRef(false);
        repository.setInvited(0);
        repository.setPosOpened(0);
        repository.setActiveRefCount(0);

        userDB.add(repository);
    }

    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
