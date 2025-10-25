package com.plovdev.bot.modules.messages;

import com.plovdev.bot.bots.*;
import com.plovdev.bot.modules.beerjes.Position;
import com.plovdev.bot.modules.beerjes.TradeService;
import com.plovdev.bot.modules.databases.*;
import com.plovdev.bot.modules.exceptions.ApiException;
import com.plovdev.bot.modules.models.PositionsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.plovdev.bot.bots.CommandHandler.*;
import static com.plovdev.bot.bots.Utils.sendUnreg;

public class Messager {
    private final TelegramLongPollingBot bot;
    private final LanguageManager manager = new LanguageManager();
    private final Logger logger = LoggerFactory.getLogger("Messager");
    private final UserDB userDB = new UserDB();
    private final BlanksDB blanksDB = new BlanksDB();
    private final StateMachine machine = new StateMachine();
    private final ProfileEditer profileEditer;
    private final ReferralDB referralDB = new ReferralDB();
    private final List<String> statuses = List.of("ACTIVE");
    private final List<String> statesToCancel = List.of("getNewApiKey:", "");

    public Messager(TelegramLongPollingBot bot) {
        this.bot = bot;
        profileEditer = new ProfileEditer(bot);
        CommandHandler.setGlobalFilter(a -> !String.valueOf(a.getChatId()).startsWith("-"));
    }

    public void registerComands() {
        registerComand("/start", (update, msg, from, chatId, text, r) -> {
            try {
                Utils.saveRepo(from, "none", userDB, "self");
            } catch (ApiException e) {
                bot.execute(new SendMessage(chatId, "ERROR"));
                return;
            }
            if (text.contains(" ")) {
                String uid = text.substring(text.indexOf(' ')).trim();
                UserEntity ref = (UserEntity) userDB.get(uid);
                int inv = ref.getInvited() + 1;
                userDB.update("invited", String.valueOf(inv), uid);
                userDB.update("referral", uid, chatId);
            }
            SendMessage message = new SendMessage(chatId, manager.getText(null, "start"));

            Button rus = new Button("\uD83C\uDDF7\uD83C\uDDFA РУ", "RUS");
            rus.setActionListener(((update1, msg1, from1, chatId1, text1, repository) -> onButtonRusPressed(from1, update1.getCallbackQuery().getMessage())));

            Button eng = new Button("\uD83C\uDDEC\uD83C\uDDE7 EN", "ENG");
            eng.setActionListener(((update1, msg1, from1, chatId1, text1, repository) -> onButtonEngPressed(from1, update1.getCallbackQuery().getMessage())));

            Button mdl = new Button("\uD83C\uDDF2\uD83C\uDDE9 MD", "MDL");
            mdl.setActionListener(((update1, msg1, from1, chatId1, text1, repository) -> onButtonMdlPressed(from1, update1.getCallbackQuery().getMessage())));

            message.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(rus, eng, mdl))));
            bot.execute(message);
        });

        registerComand("Мой реферальный код", ((update, msg, from, chatId, text, r) -> {
            UserEntity repository = (UserEntity) r;
            if (statuses.contains(repository.getStatus())) {
                SendMessage referral = new SendMessage(chatId, manager.getText(repository.getLanguage(), "referal").replace("{...}", repository.getTgId()));
                bot.execute(referral);
            } else sendUnreg(bot, repository, chatId);
        }), "My Referral Code", "Codul meu de afiliat");

        registerComand("Профиль", ((update, message, from, chatId, text, repo) -> bot.execute(profileEditer.getProfile((UserEntity) repo, chatId, manager))), "Profile", "Profilul");

        registerComand("/help", ((update, msg, from, chatId, text, r) -> {
            UserEntity repository = (UserEntity) r;
            if (repository.getStatus() != null && statuses.contains(repository.getStatus())) {
                if (!repository.getState().equals("none")) return;
                SendMessage message = new SendMessage(chatId, manager.getText(repository.getLanguage(), "help"));

                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton(manager.getText(repository.getLanguage(), "reply/profileButton")));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton(manager.getText(repository.getLanguage(), "reply/positionsButton")));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton(manager.getText(repository.getLanguage(), "reply/historyButton")));

                KeyboardRow row4 = new KeyboardRow();
                row4.add(new KeyboardButton(manager.getText(repository.getLanguage(), "reply/referralButton")));

                List<KeyboardRow> rows = new ArrayList<>(List.of(row1, row2, row3, row4));

                message.setReplyMarkup(new ReplyKeyboardMarkup(rows));
                bot.execute(message);
            } else sendUnreg(bot, repository, chatId);
        }));
        registerComand("/cancel", ((update, msg, from, chatId, text, r) -> {
            UserEntity repository = (UserEntity) r;
            if (repository != null) {
                if (!repository.getState().startsWith("getNewApiKey:") && !repository.getState().startsWith("getNewSecretKey:") && !repository.getState().startsWith("getNewPassPhrase:")) {
                    userDB.update("state", "none", repository.getTgId());
                    SendMessage message = new SendMessage(chatId, manager.getText(repository.getLanguage(), "cancel"));
                    bot.execute(message);
                } else {
                    SendMessage message = new SendMessage(chatId, "ERROR");
                    bot.execute(message);
                }
            } else {
                SendMessage message = new SendMessage(chatId, "ERROR");
                bot.execute(message);
            }
        }));


        registerComand("Позиции", ((update, msg, from, chatId, text, r) -> {
            UserEntity repository = (UserEntity) r;
            if (repository.getStatus() != null && statuses.contains(repository.getStatus())) {
                TradeService ts = repository.getUserBeerj();
                String lang = repository.getLanguage();

                List<Position> poss = ts.getPositions(repository);
                Button inMessage = new Button(manager.getText(lang, "getOrdersInMessage"), "GET_ORDERS-IN-MESSAGE");

                ExportPositions.exportCurrent("open-positions" + chatId + ".xlsx", manager.getText(lang, "positions/opens"), List.of("PAIR", "DIRECTION", "Enter", "Leverage", "PnL"), poss);
                SendDocument document = new SendDocument(chatId, new InputFile(new File("open-positions" + chatId + ".xlsx")));
                document.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(inMessage))));

                inMessage.setActionListener(((update1, message, from1, chatId1, text1, repository1) -> {
                    DeleteMessage dm = new DeleteMessage(chatId1, message.getMessageId());
                    bot.execute(dm);

                    UserEntity entity = (UserEntity) repository1;
                    StringBuilder builder = new StringBuilder();
                    poss.stream().limit(20).forEach(e -> {
                        String pair = "<b><code>" + e.getSymbol() + "</code></b>";
                        String pnl = "<b>PnL:</b><code>" + e.getUnrealizedPL() + "</code>%\n";

                        builder.append(String.join(" | ", pair, pnl));
                    });

                    SendMessage positions = new SendMessage(chatId1, String.format(manager.getText(entity.getLanguage(), "currentPositions"), builder, poss.size(), entity.getPositions()));
                    positions.setParseMode("HTML");
                    bot.execute(positions);
                }));

                bot.execute(document);
                Files.delete(Path.of("open-positions" + chatId + ".xlsx"));
            } else sendUnreg(bot, repository, chatId);
        }), "Positions", "Poziții");

        registerComand("История сделок", ((update, msg, from, chatId, text, r) -> {
            UserEntity repository = (UserEntity) r;
            String status = repository.getStatus();
            if (status != null && statuses.contains(status)) {
                String lang = repository.getLanguage();
                TradeService ts = repository.getUserBeerj();

                List<PositionsModel> poss = ts.getHistoryPositions(repository);
                System.err.println(poss);
                Button inMessage = new Button(manager.getText(lang, "getOrdersInMessage"), "GET_ORDERS-IN-MESSAGE-CLOSE");

                ExportPositions.exportHistory("close-positions" + chatId + ".xlsx", manager.getText(lang, "positions/closes"), List.of("PAIR", "DIRECTION", "OPEN", "CLOSE", "TOTAL"), poss);
                SendDocument document = new SendDocument(chatId, new InputFile(new File("close-positions" + chatId + ".xlsx")));
                document.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(inMessage))));

                inMessage.setActionListener(((update1, message, from1, chatId1, text1, repository1) -> {
                    DeleteMessage dm = new DeleteMessage(chatId1, message.getMessageId());
                    bot.execute(dm);

                    UserEntity entity = (UserEntity) repository1;
                    StringBuilder builder = new StringBuilder();
                    poss.stream().limit(10).forEach(e -> {
                        String pair = "<b><code>" + e.getPair() + "</code></b>";
                        String close = "<b>PnL:</b><code>" + e.getPnl() + "</code>%";
                        String total = "<code>" + e.getTotal() + "</code>\n";

                        builder.append(String.join(" | ", pair, close, total));
                    });

                    SendMessage positions = new SendMessage(chatId1, String.format(manager.getText(entity.getLanguage(), "closePositions"), builder, poss.size(), entity.getPositions()));
                    positions.setParseMode("HTML");
                    bot.execute(positions);
                }));

                bot.execute(document);
                Files.delete(Path.of("close-positions" + chatId + ".xlsx"));
            } else sendUnreg(bot, repository, chatId);
        }), "Trade History", "Istoricul tranzacțiilor");


        Stater stater = new Stater(userDB, blanksDB, manager, bot);
        stater.addStates(machine);
    }

    public void notifyBotComands(Update update) {
        Message message = update.getMessage();
        User from = message.getFrom();

        boolean isExecuted = notifyComands(update);

        if (!isExecuted) {
            UserEntity repository = (UserEntity) userDB.get(from.getId().toString());

            String state = repository.getState();

            try {
                if (!state.equals("none")) {
                    TemplatesHandler.handle(message, bot);
                    SendHandler.handle(message, bot);
                    machine.notifyStates(state, update);
                }
            } catch (Exception e) {
                logger.error("Произошла ошибка обработки состояний пользователя: {}", e.getMessage());
            }
        }
    }

    public void notifyBotButtons(Update update) {
        notifyButtons(update);
    }

    private void onButtonRusPressed(User from, Message message) throws Exception {
        EditMessageText sm = new EditMessageText();
        sm.setMessageId(message.getMessageId());
        sm.setDisableWebPagePreview(true);
        sm.setChatId(message.getChatId());
        sm.setText(manager.getText("ru", "license"));
        userDB.update("language", "ru", from.getId().toString());

        sm.setReplyMarkup(lcsButtons("ru"));
        bot.execute(sm);
    }

    private void onButtonEngPressed(User from, Message message) throws Exception {
        EditMessageText sm = new EditMessageText();
        sm.setMessageId(message.getMessageId());
        sm.setDisableWebPagePreview(true);
        sm.setChatId(message.getChatId());
        sm.setText(manager.getText("en", "license"));
        userDB.update("language", "en", from.getId().toString());

        sm.setReplyMarkup(lcsButtons("en"));
        bot.execute(sm);
    }

    private void onButtonMdlPressed(User from, Message message) throws Exception {
        EditMessageText sm = new EditMessageText();
        sm.setParseMode("HTML");
        sm.setDisableWebPagePreview(true);
        sm.setMessageId(message.getMessageId());
        sm.setChatId(message.getChatId());
        sm.setText(manager.getText("md", "license"));
        userDB.update("language", "md", from.getId().toString());

        sm.setReplyMarkup(lcsButtons("md"));
        bot.execute(sm);
    }

    private InlineKeyboardMarkup lcsButtons(String lang) {
        Button accept = new Button(manager.getText(lang, "accept"), "ACCPET");
        accept.setActionListener(((update, msg, from, chatId, text, repository) -> {
            UserEntity entity = (UserEntity) repository;
            EditMessageText et = new EditMessageText();
            et.setParseMode("HTML");
            et.setMessageId(msg.getMessageId());
            et.setChatId(msg.getChatId());
            et.setText(manager.getText(entity.getLanguage(), "beerj-select"));

            Button bitget = new Button("BitGet", "BITGET");
            bitget.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                UserEntity user = (UserEntity) repository1;
                userDB.update("beerj", "bitget", from1.getId().toString());
                sendGetUid(update1, user.getLanguage(), from1);
            }));

            Button bitunix = new Button("BitUnix", "BITUNIX");
            bitunix.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                UserEntity user = (UserEntity) repository1;
                userDB.update("beerj", "bitunix", from1.getId().toString());
                sendGetUid(update1, user.getLanguage(), from1);
            }));

            et.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(bitget, bitunix))));
            userDB.update("state", "getBeerj", from.getId().toString());
            bot.execute(et);
        }));

        Button reject = new Button(manager.getText(lang, "reject"), "REJECT");
        reject.setActionListener(((update, msg, from1, chatId, text, repository) -> {
            UserEntity user = (UserEntity) repository;

            Message message = update.getCallbackQuery().getMessage();
            EditMessageText et = new EditMessageText();
            et.setParseMode("HTML");
            et.setMessageId(msg.getMessageId());
            et.setChatId(msg.getChatId());
            et.setText(manager.getText(user.getLanguage(), "lcsrej"));
            userDB.remove(from1.getId().toString());

            bot.execute(et);
        }));

        return new InlineKeyboardMarkup(List.of(List.of(accept, reject)));
    }

    private void sendGetUid(Update update, String lang, User from) throws Exception {
        Message message = update.getCallbackQuery().getMessage();
        EditMessageText et = new EditMessageText();
        et.setParseMode("HTML");
        et.setMessageId(message.getMessageId());
        et.setChatId(message.getChatId());
        et.setText(manager.getText(lang, "lcsacc"));
        userDB.update("state", "getUid:" + from.getId(), from.getId().toString());
        bot.execute(et);
    }
}