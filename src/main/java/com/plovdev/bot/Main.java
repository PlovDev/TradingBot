package com.plovdev.bot;

import com.plovdev.bot.main.Bot;
import com.plovdev.bot.modules.server.WebHookListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    // Создаем логгер
    private static final Logger logger = LoggerFactory.getLogger("TradingBot");

    /**
     * Точка входа в нашего бота.
     * @param args - аргументы командной строки.
     */
    public static void main(String[] args) {
        System.out.println("=====B-800=====");
        creates();
        WebHookListener listener = new WebHookListener();
        listener.startServer();

        try {
            logger.info("Пытаемся запустить бота...");
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new Bot());
            logger.info("Бот успешно запущен!");
        } catch (Exception e) {
            logger.error("Ошибка запуска бота: {}", e.getMessage());
        }
    }
    private static void creates() {
        //UserDB d = new UserDB();
    }
}
