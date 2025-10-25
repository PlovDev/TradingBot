package com.plovdev.bot.modules.messages;

import com.plovdev.bot.bots.Button;
import com.plovdev.bot.bots.LanguageManager;
import com.plovdev.bot.modules.databases.UserDB;
import com.plovdev.bot.modules.databases.UserEntity;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class UserEditer extends SendMessage {
    private final UserDB userDB = new UserDB();
    private final LanguageManager manager = new LanguageManager();
    private final TelegramLongPollingBot bot;
    private final List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
    public UserEditer(UserEntity repository, String cid, TelegramLongPollingBot b) {
        super();
        setChatId(cid);
        setReplyMarkup(createButtons(repository));
        bot = b;
    }
    public UserEditer(UserEntity repository, TelegramLongPollingBot b) {
        setReplyMarkup(createButtons(repository));
        bot = b;
    }

    private InlineKeyboardMarkup createButtons(UserEntity repository) {
        String status = repository.getStatus();
        String user = repository.getTgId();
        boolean isBlocked = status.contains("BLOCKED");
        boolean isPause = status.contains("PAUSED");
        setText("Редактирование пользователя.\n\nTgID: " + repository.getTgId() + "\nUsername: " + repository.getTgName());
        Button possize = new Button("Размер позиции", "POS_SIZE:" + repository.getTgId());
        possize.setActionListener(((update, message, from, chatId, text, repo) -> {
            UserEntity r = (UserEntity) repo;
            String input = manager.getText(repository.getLanguage(), "selectSumOrProc");
            EditMessageText editSize = new EditMessageText(String.format(manager.getText(r.getLanguage(), "getPosOrPlech"), input));
            editSize.setChatId(chatId);
            editSize.setParseMode("HTML");
            editSize.setMessageId(message.getMessageId());
            userDB.update("state", "getNewPosOrPlech", from.getId().toString());

            Button sum = new Button(manager.getText(r.getLanguage(), "sum"), "SET_SUM");
            sum.setActionListener(((update1, msg, from1, chatId1, text1, repository1) -> {
                userDB.update("state", "getNewSum:" + user, chatId1);
                userDB.update("variant", "sum", user);

                EditMessageText edit = new EditMessageText(manager.getText(r.getLanguage(), "getSum"));
                edit.setChatId(chatId1);
                edit.setParseMode("HTML");
                edit.setMessageId(msg.getMessageId());
                bot.execute(edit);
            }));

            Button proc = new Button(manager.getText(r.getLanguage(), "proc"), "SET_PROC");
            proc.setActionListener(((update1, msg, from1, chatId1, text1, repository1) -> {
                userDB.update("state", "getNewProc:" + user, chatId1);
                userDB.update("variant", "proc", user);

                EditMessageText edit = new EditMessageText(manager.getText(r.getLanguage(), "getProc"));
                edit.setParseMode("HTML");
                edit.setChatId(chatId1);
                edit.setMessageId(msg.getMessageId());
                bot.execute(edit);
            }));

            editSize.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(sum, proc))));
            bot.execute(editSize);
        }));



        Button exipry = new Button("Срок доступа", "EXPIRY_DATE:" + repository.getTgId());
        exipry.setActionListener(((update, message, from, chatId, text, repo) -> {
            UserEntity r = (UserEntity) repo;
            EditMessageText edit = new EditMessageText(manager.getText(r.getLanguage(),"setProfile/getNewExpr"));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());
            userDB.update("state", "getNewExpr:"+user, chatId);

            Button unl = new Button("Unlimit", "SET_UNLIMIT:" + repository.getTgId());
            unl.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                userDB.update("state", "none", chatId1);
                userDB.update("expiry", "unlimited", user);
                EditMessageText e = new EditMessageText("Безлимит выдан!");
                e.setChatId(chatId);
                e.setMessageId(message1.getMessageId());
                bot.execute(e);
            }));

            edit.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(unl))));

            bot.execute(edit);
        }));

        Button positions = new Button("Количество позиций", "POSITIONS:" + repository.getTgId());
        positions.setActionListener(((update, message, from, chatId, text, repo) -> {
            UserEntity r = (UserEntity) repo;
            EditMessageText edit = new EditMessageText(manager.getText(r.getLanguage(),"setProfile/getNewPos"));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());
            userDB.update("state", "getNewPos:"+repository.getTgId(), chatId);

            String poss = manager.getText(repository.getLanguage(), "getPositions");
            Button all = new Button(manager.getText(repository.getLanguage(), "allPos"), "ALL");
            all.setActionListener(((update1, msg, from1, chatId1, text1, repository1) -> {
                EditMessageText et = new EditMessageText(manager.getText(repository.getLanguage(), "onlyExpTraders"));
                et.setReplyMarkup(getMarkup(poss, repository.getLanguage(), user, chatId1));
                et.setChatId(from1.getId().toString());
                et.setMessageId(msg.getMessageId());
                bot.execute(et);
            }));


            edit.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(all))));

            bot.execute(edit);
        }));

        Button plecho = new Button("Плечо", "PLECHO:" + repository.getTgId());
        plecho.setActionListener(((update, message, from, chatId, text, repo) -> {
            UserEntity r = (UserEntity) repo;
            EditMessageText edit = new EditMessageText(manager.getText(r.getLanguage(),"setProfile/getNewPlecho"));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());
            userDB.update("state", "getNewPlecho:"+repository.getTgId(), chatId);

            bot.execute(edit);
        }));

        Button lang = new Button("Язык", "LNGUAGE:" + repository.getTgId());
        lang.setActionListener(((update, message, from, chatId, text, repo) -> {
            UserEntity r = (UserEntity) repo;
            EditMessageText edit = new EditMessageText(manager.getText(r.getLanguage(),"setProfile/getNewLang"));
            edit.setReplyMarkup(getSetLang(user));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());

            bot.execute(edit);
        }));
        Button keys = new Button("API ключи", "API_KEYS:" + repository.getTgId());
        keys.setActionListener(((update, message, from, chatId, text, repo) -> {
            UserEntity r = (UserEntity) repo;
            EditMessageText edit = new EditMessageText(manager.getText(r.getLanguage(),"getApiKey"));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());
            userDB.update("state", "getNewApiKey:"+repository.getTgId(), chatId);

            bot.execute(edit);
        }));

        Button ban = new Button(isBlocked?"Разблокировать" : "Заблокировать", "BLOCK:" + repository.getTgId());
        ban.setActionListener(((update, message, from, chatId, text, repository1) -> {
            userDB.update("status", isBlocked ? "ACTIVE":"BLOCKED", user);
            bot.execute(new SendMessage(chatId, "Изменение успешно"));
            UserEntity entity = (UserEntity) userDB.get(text.substring(text.lastIndexOf(':')+1));
            boolean newIsBlocked = entity.getStatus().contains("BLOCKED");
            SendMessage block = new SendMessage(user, newIsBlocked?"Вас заблокировали":"вас разблокировали");
            bot.execute(block);
        }));


        Button mute = new Button(isPause ? "Продолжить" : "На паузу", "PAUSE:" + repository.getTgId());
        mute.setActionListener(((update, message, from, chatId, text, repository1) -> {
            userDB.update("status", isPause ? "ACTIVE":"PAUSED", user);
            bot.execute(new SendMessage(chatId, "Изменение успешно"));
            UserEntity entity = (UserEntity) userDB.get(text.substring(text.lastIndexOf(':')+1));
            boolean newIsPaused = entity.getStatus().contains("PAUSED");
            SendMessage block = new SendMessage(user, newIsPaused?"Вас поставили на паузу":"вас сняли с паузы");
            bot.execute(block);
        }));

        buttons.add(List.of(possize, exipry));
        buttons.add(List.of(positions, plecho));


        if (repository.getRegVariant().equals("help")) {
            buttons.add(List.of(lang, keys));
        }
        buttons.add(List.of(ban, mute));

        return (new InlineKeyboardMarkup(buttons));
    }


    private InlineKeyboardMarkup getSetLang(String user) {
        Button ru = new Button("Русский", "RUSSIAN", ((update, message, from, chatId, text, repo) -> {
            UserEntity repository = (UserEntity) repo;
            userDB.update("language", "ru", user);
            bot.execute(new SendMessage(chatId, manager.getText(repository.getLanguage(),"setProfile/changed")));
        }));


        Button en = new Button("English", "Englishh", ((update, message, from, chatId, text, repo) -> {
            UserEntity repository = (UserEntity) repo;
            userDB.update("language", "en", user);
            bot.execute(new SendMessage(chatId, manager.getText(repository.getLanguage(),"setProfile/changed")));
        }));


        Button md = new Button("Moldva", "Moldava", ((update, message, from, chatId, text, repo) -> {
            UserEntity repository = (UserEntity) repo;
            userDB.update("language", "md", user);
            bot.execute(new SendMessage(chatId, manager.getText(repository.getLanguage(),"setProfile/changed")));
        }));

        return new InlineKeyboardMarkup(List.of(List.of(ru, en, md)));
    }

    private InlineKeyboardMarkup getMarkup(String pos, String lang, String who, String fromWho) {
        Button cancel = new Button(manager.getText(lang, "cancelAllPos"), "ACC_ALL_POS");
        Button accept = new Button(manager.getText(lang, "acceptAllPos"), "CANCEL_ALL_POS");

        cancel.setActionListener(((update, msg, from, chatId, text, repository) -> {
            EditMessageText txt = new EditMessageText(pos);
            txt.setParseMode("HTML");
            txt.setChatId(from.getId().toString());
            txt.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

            userDB.update("state", "getNewPos:"+who, fromWho);

            bot.execute(txt);
        }));

        accept.setActionListener(((update, msg, from, chatId, text, repository) -> {
            SendMessage message = new SendMessage(from.getId().toString(), manager.getText(lang, "setProfile/changed"));
            message.setParseMode("HTML");
            bot.execute(message);

            EditMessageText txt = new EditMessageText(manager.getText(lang, "onlyExpTradersNext"));
            txt.setParseMode("HTML");
            txt.setChatId(from.getId().toString());
            txt.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

            userDB.update("state", "none", fromWho);
            userDB.update("positions", "all", who);

            bot.execute(txt);
        }));

        return new InlineKeyboardMarkup(List.of(List.of(accept, cancel)));
    }
}