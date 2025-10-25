package com.plovdev.bot.main;

import com.plovdev.bot.bots.ExpiryListener;
import com.plovdev.bot.bots.LanguageManager;
import com.plovdev.bot.bots.Utils;
import com.plovdev.bot.modules.beerjes.SignalOpener;
import com.plovdev.bot.modules.beerjes.SignalQueue;
import com.plovdev.bot.modules.messages.AdminComands;
import com.plovdev.bot.modules.messages.Messager;
import com.plovdev.bot.modules.parsers.Signal;
import com.plovdev.bot.modules.parsers.TelegramSignalParser;
import com.plovdev.bot.modules.parsers.TradingViewSignalParser;
import com.plovdev.bot.modules.parsers.notifies.SignalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.prefs.Preferences;

public class Bot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(Bot.class);
    private final Preferences prefs = Preferences.userRoot().node("TradingBot");
    private final SignalQueue signalQueue = new SignalQueue();

    @Override
    public String getBotToken() {
        return Utils.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return "TradingBot";
    }

    private final Messager messager = new Messager(this);
    private final AdminComands comands = new AdminComands(this);
    private final LanguageManager manager = new LanguageManager();
    private final SignalOpener signalOpener = new SignalOpener(this);


    public Bot() {
        messager.registerComands(); // добавляем команды для бота.
        comands.registerComands();
        signalQueue.setNextSignalHandler(this::handleSignal);

        SignalListener.addSignalListener(((signal) -> {
            log.info("Getted new signal, symbol: {}", signal.getSymbol());
            signalQueue.add(signal);
        }));

        ExpiryListener.addListener((id, lang) -> {
            try {
                execute(new SendMessage(id, manager.getText(lang, "expireOut")));
            } catch (Exception e) {
                log.error("Failed to send expiry notifications to user {}:", id, e);
            }
        });
    }

    private void handleSignal(Signal signal) {
        signalOpener.openSignal(signal)
                .thenAccept(results -> {
                    log.info("✅ Signal by {}, {}, execution completed for {} users", signal.getSymbol(), signal.getDirection(), results.size());
                    System.out.println("\n\n\n\n\n");
                })
                .exceptionally(e -> {
                    log.error("❌ Signal execution failed: {}", e.getMessage(), e);
                    System.out.println("\n\n\n\n\n");
                    return null;
                })
                .whenComplete((v, ex) -> {
                    signalQueue.onSignalProcessComplete(); // ← очередь разблокируется
                });
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            messager.notifyBotComands(update); // уведомляем команд
        } else if (update.hasChannelPost() && update.getChannelPost().hasText()) {
            Message message = update.getChannelPost();
            String chatId = message.getChatId().toString();
            String text = message.getText();


            if (chatId.equals(prefs.get("chanel-id", "-1002729649638"))) {
                if (TelegramSignalParser.validate(text)) SignalListener.notifySignals(TelegramSignalParser.parse(text));
            }
            if (chatId.equals(prefs.get("tv-chanel-id", "-1002729649638"))) {
                if (TradingViewSignalParser.validate(text)) {
                    SignalListener.notifySignals(TradingViewSignalParser.parse(text));
                }
            }
        } else if (update.hasCallbackQuery()) {
            messager.notifyBotButtons(update); // уведомляем калбеки.
        }
    }
}
