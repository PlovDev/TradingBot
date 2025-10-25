package com.plovdev.bot.modules.messages;

import com.plovdev.bot.bots.Button;
import com.plovdev.bot.bots.LanguageManager;
import com.plovdev.bot.modules.databases.UserDB;
import com.plovdev.bot.modules.databases.UserEntity;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProfileEditer {
    private final TelegramLongPollingBot bot;
    private final UserDB userDB = new UserDB();
    private final LanguageManager manager = new LanguageManager();

    public ProfileEditer(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public List<InlineKeyboardButton> getFirstRow(String lang) {
        Button name = new Button(manager.getText(lang, "setProfile/setNameButton"), "SET_NAME");
        name.setActionListener(((update, message, from, chatId, text, repository) -> {
            EditMessageText edit = new EditMessageText(manager.getText(lang, "setProfile/getNewName"));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());
            userDB.update("state", "getNewName:" + chatId, chatId);

            bot.execute(edit);
        }));

        Button pos = new Button(manager.getText(lang, "setProfile/setPosButton"), "SET_POS");
        pos.setActionListener(((update, message, from, chatId, text, repository) -> {
            EditMessageText edit = new EditMessageText(manager.getText(lang, "setProfile/getNewPos"));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());
            userDB.update("state", "getNewPos:" + chatId, chatId);

            String positions = manager.getText(lang, "getPositions");
            Button all = new Button(manager.getText(lang, "allPos"), "ALL");
            all.setActionListener(((update1, msg, from1, chatId1, text1, repository1) -> {
                EditMessageText et = new EditMessageText(manager.getText(lang, "onlyExpTraders"));
                et.setReplyMarkup(getMarkup(positions, lang));
                et.setChatId(from1.getId().toString());
                et.setMessageId(msg.getMessageId());
                bot.execute(et);
            }));


            edit.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(all))));

            bot.execute(edit);
        }));

        return List.of(name, pos);
    }

    public List<InlineKeyboardButton> getFourthtRow(String lang) {
        Button name = new Button(manager.getText(lang, "setProfile/setKeysButton"), "SET_KEYS");
        name.setActionListener(((update, message, from, chatId, text, repository) -> {
            EditMessageText edit = new EditMessageText(manager.getText(lang, "getApiKey"));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());
            userDB.update("state", "getNewApiKey:" + chatId, chatId);

            bot.execute(edit);
        }));

        return List.of(name);
    }


    public List<InlineKeyboardButton> getSecondRow(String lang) {
        Button name = new Button(manager.getText(lang, "setProfile/setPosSizeButton"), "SET_PO_OR_PLECH");
        name.setActionListener(((update, message, from, chatId, text, repository) -> {
            UserEntity user = (UserEntity) repository;
            EditMessageText editSize = new EditMessageText(manager.getText(lang, "getPosOrPlech"));
            editSize.setChatId(chatId);
            editSize.setParseMode("HTML");
            editSize.setMessageId(message.getMessageId());
            userDB.update("state", "getPosOrPlech", from.getId().toString());

            Button sum = new Button(manager.getText(lang, "sum"), "SET_SUM");
            sum.setActionListener(((update1, msg, from1, chatId1, text1, repository1) -> {
                userDB.update("state", "getNewSum:" + chatId1, chatId1);
                userDB.update("variant", "sum", chatId1);

                EditMessageText edit = new EditMessageText(manager.getText(lang, "getSum"));
                edit.setChatId(chatId1);
                edit.setParseMode("HTML");
                edit.setMessageId(msg.getMessageId());
                bot.execute(edit);
            }));

            Button proc = new Button(manager.getText(lang, "proc"), "SET_PROC");
            proc.setActionListener(((update1, msg, from1, chatId1, text1, repository1) -> {
                userDB.update("state", "getNewProc:" + chatId1, chatId1);
                userDB.update("variant", "proc", chatId1);

                EditMessageText edit = new EditMessageText(manager.getText(lang, "getProc"));
                edit.setParseMode("HTML");
                edit.setChatId(chatId1);
                edit.setMessageId(msg.getMessageId());
                bot.execute(edit);
            }));

            List<InlineKeyboardButton> buttons = new ArrayList<>(List.of(sum));
            if (user.getUserBeerj().getBalance(user).compareTo(new BigDecimal("1500")) >= 0) {
                buttons.add(proc);
                String input = manager.getText(user.getLanguage(), "selectSumOrProc");
                editSize.setText(String.format(manager.getText(user.getLanguage(), "getPosOrPlech"), input));
            } else {
                String userId = user.getTgId();
                String input = manager.getText(user.getLanguage(), "getOnlySum");
                editSize.setText(String.format(manager.getText(user.getLanguage(), "getPosOrPlech"), input));
//                userDB.update("state", "getNewSum:" + userId, userId);
//                userDB.update("variant", "sum", userId);
            }
            editSize.setReplyMarkup(new InlineKeyboardMarkup(List.of(buttons)));
            bot.execute(editSize);
        }));

        Button plecho = new Button(manager.getText(lang, "setProfile/setPlechoButton"), "SET_PLECHO");
        plecho.setActionListener(((update, message, from, chatId, text, repository) -> {
            EditMessageText edit = new EditMessageText(manager.getText(lang, "setProfile/getNewPlecho"));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());
            userDB.update("state", "getNewPlecho:" + chatId, chatId);

            bot.execute(edit);
        }));


        return List.of(name, plecho);
    }

    public List<InlineKeyboardButton> getThirdRow(String lang) {
        Button pos = new Button(manager.getText(lang, "setProfile/setLangButton"), "SET_LANG");
        pos.setActionListener(((update, message, from, chatId, text, repository) -> {
            EditMessageText edit = new EditMessageText(manager.getText(lang, "setProfile/getNewLang"));
            edit.setReplyMarkup(getSetLang());
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());

            bot.execute(edit);
        }));

        Button beerj = new Button(manager.getText(lang, "setProfile/setBeerjButton"), "SET_BEERJ");
        beerj.setActionListener(((update, message, from, chatId, text, repository) -> {
            EditMessageText edit = new EditMessageText(manager.getText(lang, "setProfile/getNewBeerj"));
            edit.setReplyMarkup(getSetBeerj());
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());

            bot.execute(edit);
        }));

        return List.of(pos, beerj);
    }

    public SendMessage getProfile(UserEntity repository, String chatId, LanguageManager manager) {
        SendMessage profile = new SendMessage(chatId, String.format(manager.getText(repository.getLanguage(), "profile"),
                repository.getName(),
                repository.getUID(),
                repository.getPositions(),
                repository.getPlecho(),
                repository.getVariant().equals("sum") ? repository.getSum() + "USDT" : repository.getProc() + "%",
                repository.getBeerj(),
                repository.getExpiry()
        ));

        Button editProfile = new Button(manager.getText(repository.getLanguage(), "/setProfile/@name"), "EDIT_PROFILE");
        editProfile.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
            EditMessageText edit = new EditMessageText(manager.getText(repository.getLanguage(), "/setProfile/@what"));

            Button cancel = new Button(manager.getText(repository.getLanguage(), "setProfile/cancel"), "SET_CANCEL");
            cancel.setActionListener(((update, message, from, chatId2, text, repository2) -> {
                DeleteMessage delete = new DeleteMessage(chatId2, message.getMessageId());
                bot.execute(delete);
                bot.execute(getProfile((UserEntity) repository2, chatId2, manager));
            }));

            edit.setReplyMarkup(new InlineKeyboardMarkup(List.of(getFirstRow(repository.getLanguage()), getSecondRow(repository.getLanguage()), getThirdRow(repository.getLanguage()), getFourthtRow(repository.getLanguage()), List.of(cancel))));
            edit.setChatId(chatId1);
            edit.setMessageId(message1.getMessageId());
            bot.execute(edit);
        }));

        profile.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(editProfile))));

        return profile;
    }

    public InlineKeyboardMarkup getSetLang() {
        Button ru = new Button("\uD83C\uDDF7\uD83C\uDDFA РУ", "RUSSIAN", ((update, message, from, chatId, text, repo) -> {
            userDB.update("language", "ru", chatId);
            UserEntity repository = (UserEntity) userDB.get(chatId);
            bot.execute(new SendMessage(chatId, manager.getText(repository.getLanguage(), "setProfile/changed")));
        }));


        Button en = new Button("\uD83C\uDDEC\uD83C\uDDE7 EN", "Englishh", ((update, message, from, chatId, text, repo) -> {
            userDB.update("language", "en", from.getId().toString());
            UserEntity repository = (UserEntity) userDB.get(chatId);
            bot.execute(new SendMessage(chatId, manager.getText(repository.getLanguage(), "setProfile/changed")));
        }));


        Button md = new Button("\uD83C\uDDF2\uD83C\uDDE9 MD", "Moldava", ((update, message, from, chatId, text, repo) -> {
            userDB.update("language", "md", from.getId().toString());
            UserEntity repository = (UserEntity) userDB.get(chatId);
            bot.execute(new SendMessage(chatId, manager.getText(repository.getLanguage(), "setProfile/changed")));
        }));

        return new InlineKeyboardMarkup(List.of(List.of(ru, en, md)));
    }


    public InlineKeyboardMarkup getSetBeerj() {
        Button ru = new Button("BitGet", "BGT", ((update, message, from, chatId, text, repo) -> {
            UserEntity repository = (UserEntity) repo;
            userDB.update("beerj", "bitget", from.getId().toString());

            EditMessageText edit = new EditMessageText(manager.getText(repository.getLanguage(), "getApiKey"));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());
            userDB.update("state", "getNewApiKey:" + chatId, chatId);

            bot.execute(edit);
        }));


        Button en = new Button("BitUnix", "BUX", ((update, message, from, chatId, text, repo) -> {
            UserEntity repository = (UserEntity) repo;
            userDB.update("beerj", "bitunix", from.getId().toString());

            EditMessageText edit = new EditMessageText(manager.getText(repository.getLanguage(), "getApiKey"));
            edit.setChatId(chatId);
            edit.setMessageId(message.getMessageId());
            userDB.update("state", "getNewApiKey:" + chatId, chatId);

            bot.execute(edit);
        }));

        return new InlineKeyboardMarkup(List.of(List.of(ru, en)));
    }

    private InlineKeyboardMarkup getMarkup(String pos, String lang) {
        Button cancel = new Button(manager.getText(lang, "cancelAllPos"), "ACC_ALL_POS1");
        Button accept = new Button(manager.getText(lang, "acceptAllPos"), "CANCEL_ALL_POS1");

        cancel.setActionListener(((update, msg, from, chatId, text, repository) -> {
            EditMessageText edit = new EditMessageText(manager.getText(lang, "setProfile/getNewPos"));
            edit.setChatId(chatId);
            edit.setMessageId(msg.getMessageId());
            userDB.update("state", "getNewPos:" + chatId, chatId);

            String positions = manager.getText(lang, "getPositions");
            Button all = new Button(manager.getText(lang, "allPos"), "ALL");
            all.setActionListener(((update1, message, from1, chatId1, text1, repository1) -> {
                EditMessageText et = new EditMessageText(manager.getText(lang, "onlyExpTraders"));
                et.setReplyMarkup(getMarkup(positions, lang));
                et.setChatId(from1.getId().toString());
                et.setMessageId(message.getMessageId());
                bot.execute(et);
            }));


            edit.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(all))));

            bot.execute(edit);
        }));

        accept.setActionListener(((update, msg, from, chatId, text, repository) -> {
            SendMessage message = new SendMessage(from.getId().toString(), manager.getText(lang, "setProfile/changed"));
            message.setParseMode("HTML");
            bot.execute(message);

            userDB.update("state", "none", from.getId().toString());
            userDB.update("positions", "all", from.getId().toString());
        }));

        return new InlineKeyboardMarkup(List.of(List.of(accept, cancel)));
    }
}