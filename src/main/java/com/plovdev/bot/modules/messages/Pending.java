package com.plovdev.bot.modules.messages;

import com.plovdev.bot.bots.Button;
import com.plovdev.bot.bots.LanguageManager;
import com.plovdev.bot.modules.databases.BlanksDB;
import com.plovdev.bot.modules.databases.UserDB;
import com.plovdev.bot.modules.databases.UserEntity;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public class Pending extends SendMessage {
    private final UserDB userDB = new UserDB();
    public final BlanksDB blanksDB = new BlanksDB();
    private final LanguageManager manager = new LanguageManager();

    public Pending(String chatId, String text, String userId, TelegramLongPollingBot bot, String newState, String success, String err) {
        super(chatId, text);
        setParseMode("HTML");
        System.out.println(userId + " - first in pending");
        Button accept = getAccept(userId, bot, newState, success);


        Button reject = new Button("Отклонить", "PENDINGREJECT:" + userId);

        reject.setActionListener(((update, msg, from, chatId1, text1, repository) -> {
            if (blanksDB.getByKey("state", userId).equals("waiting")) {
                UserEntity repo = (UserEntity) userDB.get(userId);
                SendMessage message = new SendMessage(userId, manager.getText(repo.getLanguage(), err));
                bot.execute(message);

                EditMessageText et = new EditMessageText("Отклонение отправлено");
                et.setChatId(from.getId().toString());
                et.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                bot.execute(et);

                if (!err.equals("referrReject")) userDB.remove(userId);

                blanksDB.upadateByKey("state", "none", userId);
                blanksDB.remove(userId);
            } else {
                EditMessageText message = new EditMessageText("Заявка уже рассмотрена");
                message.setChatId(from.getId().toString());
                message.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                bot.execute(message);
            }
        }));

        setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(accept, reject))));
    }

    private Button getAccept(String userId, TelegramLongPollingBot bot, String newState, String scs) {
        Button accept = new Button("Принять", "PENDINGACCEPT:" + userId);
        accept.setActionListener(((update, msg, from, chatId1, text1, repository) -> {
            String blankState = blanksDB.getByKey("state", userId);

            if (blankState.equals("waiting")) {
                UserEntity repo = (UserEntity) userDB.get(userId);

                SendMessage message = new SendMessage();
                if (scs.equals("regOk")) {
                    String beerj = "BitGet";
                    String phrase = "";
                    if (repo.getBeerj().equalsIgnoreCase("bitunix")) beerj = "BitUnix";
                    if (beerj.equals("BitGet")) {
                        phrase = manager.getText(repo.getLanguage(), "codePhrase");
                    }
                    message = new SendMessage(userId, String.format(manager.getText(repo.getLanguage(), scs), beerj, beerj, phrase));
                } else if (scs.equalsIgnoreCase("referrAccept")) {
                    message = new SendMessage(userId, String.format(manager.getText(repo.getLanguage(), scs), repo.getUID()));
                }
                userDB.update("state", newState, userId);
                bot.execute(message);

                EditMessageText et = new EditMessageText("Заявка принята");
                et.setChatId(from.getId().toString());
                et.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                bot.execute(et);
                blanksDB.remove(userId);
            } else {
                EditMessageText message = new EditMessageText("Заявка уже рассмотрена");
                message.setChatId(from.getId().toString());
                message.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                bot.execute(message);
            }
        }));
        return accept;
    }
}