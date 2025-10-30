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

public class SetBeerjPending extends SendMessage {
    private final UserDB userDB = new UserDB();
    public final BlanksDB blanksDB = new BlanksDB();
    private final LanguageManager manager = new LanguageManager();

    public SetBeerjPending(String chatId, String text, String userId, TelegramLongPollingBot bot) {
        super(chatId, text);
        setParseMode("HTML");
        System.out.println(userId + " - first in pending");
        Button accept = getAccept(userId, bot);


        Button reject = new Button("Отклонить", "PENDINGREJECT:" + userId);

        reject.setActionListener(((update, msg, from, chatId1, text1, repository) -> {
            if (blanksDB.getByKey("state", userId).equals("waiting")) {
                UserEntity repo = (UserEntity) userDB.get(userId);
                SendMessage message = new SendMessage(userId, manager.getText(repo.getLanguage(), "regErr"));
                bot.execute(message);

                EditMessageText et = new EditMessageText("Отклонение отправлено");
                et.setChatId(from.getId().toString());
                et.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                bot.execute(et);

                blanksDB.upadateByKey("state", "getNewUidForSetBeerj:"+userId, userId);
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

    private Button getAccept(String userId, TelegramLongPollingBot bot) {
        Button accept = new Button("Принять", "PENDINGACCEPT:" + userId);
        accept.setActionListener(((update, msg, from, chatId1, text1, repository) -> {
            String blankState = blanksDB.getByKey("state", userId);

            if (blankState.equals("waiting")) {
                UserEntity repo = (UserEntity) userDB.get(userId);

                SendMessage message = new SendMessage();
                String beerj = "BitGet";
                String phrase = "";
                if (repo.getBeerj().equalsIgnoreCase("bitunix")) beerj = "BitUnix";
                if (beerj.equals("BitGet")) {
                    phrase = manager.getText(repo.getLanguage(), "codePhrase");
                }
                message = new SendMessage(userId, String.format(manager.getText(repo.getLanguage(), "regOk"), beerj, beerj, phrase));
                userDB.update("state", "getNewApiKey:"+userId, userId);
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