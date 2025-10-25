package com.plovdev.bot.modules.messages;

import com.plovdev.bot.bots.*;
import com.plovdev.bot.modules.beerjes.Order;
import com.plovdev.bot.modules.beerjes.TradeService;
import com.plovdev.bot.modules.beerjes.utils.BeerjUtils;
import com.plovdev.bot.modules.databases.*;
import com.plovdev.bot.modules.databases.base.Entity;
import com.plovdev.bot.modules.exceptions.InvalidParametresException;
import com.plovdev.bot.modules.models.OrderResult;
import com.plovdev.bot.modules.models.SettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

import static com.plovdev.bot.bots.Utils.sendNoPerms;

public class Stater {
    private final UserDB userDB;
    private final BlanksDB blanksDB;
    private final LanguageManager manager;
    private final Logger logger;
    private final TelegramLongPollingBot bot;
    private final ReferralDB referralDB = new ReferralDB();
    private final Preferences prefs = Preferences.userRoot().node("TradingBot");
    private final SettingsService service = new SettingsService();
    private final TemplateDB templatesDB = new TemplateDB();

    public Stater(UserDB udb, BlanksDB bdb, LanguageManager m, TelegramLongPollingBot b) {
        userDB = udb;
        blanksDB = bdb;
        manager = m;
        logger = LoggerFactory.getLogger("Stater");
        bot = b;
    }


    public void addStates(StateMachine machine) {
        machine.addState(new State("getUid:", (update, message, from, chatId, text, repo, name1) -> {
            UserEntity repository = (UserEntity) userDB.get(name1.substring(name1.indexOf(':') + 1));
            UserEntity base = (UserEntity) userDB.get(chatId);

            if (Utils.isOnlyNumbers(message.getText())) {
                userDB.update("uid", message.getText(), repository.getTgId());

                if (repository.getRegVariant().equals("self")) {
                    repository.setUID(message.getText());
                    blanksDB.add(repository.getTgId(), repository.getUID(), repository.getTgName(), "waiting", "pend", repository.getBeerj());
                    SendMessage sm = new SendMessage(repository.getTgId(), manager.getText(base.getLanguage(), "validUid"));
                    bot.execute(sm);

                    userDB.update("state", "none", repository.getTgId());
                    userDB.getAll().stream()
                            .filter(e -> e.getRole().equals("admin") || e.getRole().equalsIgnoreCase("moder"))
                            .forEach(repos -> {
                                String header = "<b>Новая заявка!</b>\n\n";
                                String tgId = "ID в телеграм: <b>" + repository.getTgId() + "</b>\n";
                                String name = "Имя: " + repository.getTgName() + "\n";
                                String beerj = "Биржа: " + repository.getBeerj() + "\n";
                                String uid = "UID на бирже: <b>" + repository.getUID() + "</b>";

                                Pending pending = new Pending(repos.getTgId(), header + tgId + name + beerj + uid, repository.getTgId(), bot, "getApiKey:" + repository.getTgId(), "regOk", "regErr");
                                try {
                                    bot.execute(pending);
                                } catch (TelegramApiException e) {
                                    logger.error("Ошибка отправки заявки: {}", e.getMessage());
                                }
                            });
                } else {
                    bot.execute(new SendMessage(chatId, manager.getText(base.getLanguage(), "getApiKey")));
                    userDB.update("state", "getApiKey:" + repository.getTgId(), chatId);
                }

            } else {
                SendMessage sm = new SendMessage(base.getTgId(), manager.getText(repository.getLanguage(), "notValidUid"));
                bot.execute(sm);
            }
        }));


        machine.addState(new State("getNewApiKey:", ((update, message, from, chatId, apiKey, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            UserEntity base = (UserEntity) userDB.get(chatId);

            String userId = repository.getTgId();

            userDB.update("state", "getNewSecretKey:" + userId, chatId);

            String encApiKey = repository.getUserBeerj().getSecurityService().encrypt(apiKey);
            userDB.update("apiKey", encApiKey, userId);

            SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "getSecretKey"));
            bot.execute(sm);
        })));

        machine.addState(new State("getNewSecretKey:", ((update, message, from, chatId, secretKey, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            String userId = repository.getTgId();
            TradeService service = repository.getUserBeerj();
            UserEntity base = (UserEntity) userDB.get(chatId);

            String encApiKey = repository.getUserBeerj().getSecurityService().encrypt(secretKey);
            userDB.update("secretKey", encApiKey, userId);

            if (repository.getBeerj().equalsIgnoreCase("bitget")) {
                userDB.update("state", "getNewPassPhrase:" + userId, chatId);
                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "getPassPhrase"));
                bot.execute(sm);
            } else {
                repository.setSecretKey(encApiKey);
                if (service.checkApiKeys(repository)) {
                    userDB.update("state", "none", chatId);

                    SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "setProfile/changed"));
                    bot.execute(sm);
                } else {
                    userDB.update("state", "getNewApiKey:" + userId, chatId);
                    SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "validErr"));
                    bot.execute(sm);
                }
            }
        })));


        machine.addState(new State("getNewPassPhrase:", ((update, message, from, chatId, passPhrase, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            TradeService service = repository.getUserBeerj();
            UserEntity base = (UserEntity) userDB.get(chatId);

            String userId = repository.getTgId();

            repository.setPhrase(service.getSecurityService().encrypt(passPhrase));

            if (service.checkApiKeys(repository)) {
                userDB.update("state", "none", chatId);

                String encApiKey = repository.getUserBeerj().getSecurityService().encrypt(passPhrase);
                userDB.update("phrase", encApiKey, userId);

                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "setProfile/changed"));
                bot.execute(sm);
            } else {
                userDB.update("state", "getNewApiKey:" + userId, chatId);
                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "validErr"));
                bot.execute(sm);
            }
        })));


        machine.addState(new State("getApiKey:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            UserEntity base = (UserEntity) userDB.get(chatId);
            String userId = repository.getTgId();
            String apiKey = message.getText();

            userDB.update("state", "getSecretKey:" + userId, chatId);
            System.out.println(((UserEntity) userDB.get(userId)).getBeerj());
            String encApiKey = repository.getUserBeerj().getSecurityService().encrypt(apiKey);
            userDB.update("apiKey", encApiKey, userId);

            SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "getSecretKey"));
            bot.execute(sm);
        })));

        machine.addState(new State("getSecretKey:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            TradeService service = repository.getUserBeerj();
            String userId = repository.getTgId();
            String secretKey = message.getText();
            UserEntity base = (UserEntity) userDB.get(chatId);

            String encApiKey = repository.getUserBeerj().getSecurityService().encrypt(secretKey);
            userDB.update("secretKey", encApiKey, userId);

            if (repository.getBeerj().equalsIgnoreCase("bitget")) {
                userDB.update("state", "getPassPhrase:" + userId, chatId);
                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "getPassPhrase"));
                bot.execute(sm);
            } else {
                repository.setSecretKey(encApiKey);
                if (service.checkApiKeys(repository)) {
                    userDB.update("state", "getName:" + userId, chatId);
                    SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "validOk"));
                    bot.execute(sm);
                } else {
                    userDB.update("state", "getApiKey:" + userId, chatId);
                    SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "validErr"));
                    bot.execute(sm);
                }
            }
        })));


        machine.addState(new State("getPassPhrase:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            TradeService service = repository.getUserBeerj();
            UserEntity base = (UserEntity) userDB.get(chatId);

            String userId = repository.getTgId();

            repository.setPhrase(service.getSecurityService().encrypt(text));
            try {
                if (service.checkApiKeys(repository)) {
                    userDB.update("state", "getName:" + userId, chatId);

                    userDB.update("phrase", service.getSecurityService().encrypt(text), userId);

                    SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "validOk"));
                    bot.execute(sm);
                } else {
                    userDB.update("state", "getApiKey:" + userId, chatId);
                    SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "validErr"));
                    bot.execute(sm);
                }
            } catch (Exception e) {
                e.printStackTrace();
                userDB.update("state", "getApiKey:" + userId, chatId);
                SendMessage sm = new SendMessage(chatId, manager.getText(repository.getLanguage(), "validErr"));
                bot.execute(sm);
            }
        })));

        machine.addState(new State("getName:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            String userId = repository.getTgId();
            UserEntity base = (UserEntity) userDB.get(chatId);

            userDB.update("state", "getPositions:" + userId, chatId);
            userDB.update("name", message.getText(), userId);

            String positions = manager.getText(base.getLanguage(), "getPositions");
            SendMessage sm = new SendMessage(chatId, positions);

            Button all = new Button(manager.getText(base.getLanguage(), "allPos"), "ALL");
            all.setActionListener(((update1, msg, from1, chatId1, text1, repository1) -> {
                EditMessageText et = new EditMessageText(manager.getText(base.getLanguage(), "onlyExpTraders"));
                et.setReplyMarkup(getMarkup(positions, base.getLanguage(), userId));
                et.setChatId(chatId1);
                et.setMessageId(msg.getMessageId());
                bot.execute(et);
            }));


            sm.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(all))));
            sm.setParseMode("HTML");
            bot.execute(sm);
        })));


        machine.addState(new State("getNewName:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            String userId = repository.getTgId();
            UserEntity base = (UserEntity) userDB.get(chatId);

            userDB.update("state", "none", chatId);
            userDB.update("name", text, userId);


            SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "setProfile/changed"));
            sm.setParseMode("HTML");
            bot.execute(sm);
        })));


        machine.addState(new State("getNewExpr:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            String userId = repository.getTgId();
            UserEntity base = (UserEntity) userDB.get(chatId);

            userDB.update("state", "none", chatId);
            userDB.update("expiry", text, userId);


            SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "setProfile/changed"));
            sm.setParseMode("HTML");
            bot.execute(sm);
        })));

        machine.addState(new State("getNewPos:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            String userId = repository.getTgId();
            UserEntity base = (UserEntity) userDB.get(chatId);

            if (Utils.isOnlyNumbers(text)) {
                userDB.update("state", "none", chatId);
                userDB.update("positions", text, userId);


                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "setProfile/changed"));
                sm.setParseMode("HTML");
                bot.execute(sm);
            } else {
                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "nonNumber"));
                bot.execute(sm);
            }
        })));

        machine.addState(new State("getNewPlecho:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            String userId = repository.getTgId();
            UserEntity base = (UserEntity) userDB.get(chatId);

            if (Utils.isOnlyNumbers(text)) {
                System.err.println(userId);
                userDB.update("state", "none", chatId);
                userDB.update("plecho", text, userId);


                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "setProfile/changed"));
                sm.setParseMode("HTML");
                bot.execute(sm);
            } else {
                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "nonNumber"));
                bot.execute(sm);
            }
        })));

        machine.addState(new State("getPositions:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            UserEntity base = (UserEntity) userDB.get(chatId);

            if (Utils.isOnlyNumbers(message.getText())) {
                String userId = repository.getTgId();

                userDB.update("state", "getPlecho:" + userId, chatId);
                userDB.update("positions", message.getText(), userId);

                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "getPlecho"));
                sm.setParseMode("HTML");
                bot.execute(sm);
            } else {
                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "nonNumber"));
                bot.execute(sm);
            }
        })));

        machine.addState(new State("getPlecho:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            TradeService service = repository.getUserBeerj();
            UserEntity base = (UserEntity) userDB.get(chatId);

            if (Utils.isOnlyNumbers(message.getText())) {
                String userId = repository.getTgId();

                userDB.update("state", "getPosOrPlech:" + userId, chatId);
                userDB.update("plecho", message.getText(), userId);

                Button sum = new Button(manager.getText(base.getLanguage(), "sum"), "SUM");
                sum.setActionListener(((update1, msg, from1, chatId1, text1, repository1) -> {
                    userDB.update("state", "getSum:" + userId, chatId1);
                    userDB.update("variant", "sum", userId);

                    EditMessageText edit = new EditMessageText(manager.getText(base.getLanguage(), "getSum"));
                    edit.setChatId(chatId1);
                    edit.setParseMode("HTML");
                    edit.setMessageId(msg.getMessageId());
                    bot.execute(edit);
                }));

                Button proc = new Button(manager.getText(base.getLanguage(), "proc"), "PROC");
                proc.setActionListener(((update1, msg, from1, chatId1, text1, repository1) -> {
                    userDB.update("state", "getProc:" + userId, chatId1);
                    userDB.update("variant", "proc", userId);

                    EditMessageText edit = new EditMessageText(manager.getText(base.getLanguage(), "getProc"));
                    edit.setParseMode("HTML");
                    edit.setChatId(chatId1);
                    edit.setMessageId(msg.getMessageId());
                    bot.execute(edit);
                }));


                SendMessage sm = new SendMessage();
                sm.setChatId(chatId);
                sm.setParseMode("HTML");

                List<InlineKeyboardButton> buttons = new ArrayList<>(List.of(sum));
                if (service.getBalance(repository).compareTo(new BigDecimal("1500")) >= 0) {
                    buttons.add(proc);
                    String input = manager.getText(base.getLanguage(), "selectSumOrProc");
                    sm.setText(String.format(manager.getText(base.getLanguage(), "getPosOrPlech"), input));
                } else {
                    String input = manager.getText(base.getLanguage(), "getOnlySum");
                    sm.setText(String.format(manager.getText(base.getLanguage(), "getPosOrPlech"), input));
//                    userDB.update("state", "getSum:" + userId, chatId);
//                    userDB.update("variant", "sum", userId);
                }
                sm.setReplyMarkup(new InlineKeyboardMarkup(List.of(buttons)));
                bot.execute(sm);
            } else {
                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "nonNumber"));
                bot.execute(sm);
            }
        })));

        machine.addState(new State("getSum:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            UserEntity base = (UserEntity) userDB.get(chatId);

            try {
                if (Utils.isOnlyNumbers(message.getText())) {
                    String userId = repository.getTgId();

                    if (Long.parseLong(message.getText()) >= 10) {
                        userDB.update("state", "none", userId);
                        userDB.update("state", "none", chatId);
                        userDB.update("status", "ACTIVE", userId);
                        userDB.update("sum", message.getText(), userId);

                        if (repository.getRegVariant().equals("help"))
                            bot.execute(new SendMessage(chatId, "Новый пользователь успешно добавлен в бота!"));
                        try {
                            SendMessage sm = new SendMessage(userId, manager.getText(repository.getLanguage(), "regDone"));
                            bot.execute(sm);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    } else {
                        SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "minSum"));
                        bot.execute(sm);
                    }
                } else {
                    SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "nonNumber"));
                    bot.execute(sm);
                }
            } catch (Exception e) {
                logger.error("Not a number");
            }
        })));

        machine.addState(new State("getProc:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            TradeService service = repository.getUserBeerj();
            UserEntity base = (UserEntity) userDB.get(chatId);

            if (Utils.isOnlyNumbers(message.getText())) {
                if (BeerjUtils.getPercent(new BigDecimal(text), service.getBalance(repository)).compareTo(BigDecimal.TEN) >= 0) {
                    String userId = repository.getTgId();

                    userDB.update("state", "none", userId);
                    userDB.update("state", "none", chatId);
                    userDB.update("proc", message.getText(), userId);
                    userDB.update("status", "ACTIVE", userId);

                    if (repository.getRegVariant().equals("help"))
                        bot.execute(new SendMessage(chatId, "Новый пользователь успешно добавлен в бота!"));
                    try {
                        SendMessage sm = new SendMessage(userId, manager.getText(repository.getLanguage(), "regDone"));
                        bot.execute(sm);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "minSum"));
                    bot.execute(sm);
                }
            } else {
                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "nonNumber"));
                bot.execute(sm);
            }
        })));


        machine.addState(new State("getNewSum:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            String userId = repository.getTgId();
            UserEntity base = (UserEntity) userDB.get(chatId);
            try {
                if (Utils.isOnlyNumbers(text)) {
                    if (Long.parseLong(text) >= 10) {
                        userDB.update("state", "none", chatId);
                        userDB.update("sum", text, userId);

                        SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "setProfile/changed"));
                        bot.execute(sm);
                    } else {
                        SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "minSum"));
                        bot.execute(sm);
                    }
                } else {
                    SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "nonNumber"));
                    bot.execute(sm);
                }
            } catch (Exception e) {
                logger.error("New Sum id not a number");
            }
        })));

        machine.addState(new State("getNewProc:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) userDB.get(name.substring(name.indexOf(':') + 1));
            String userId = repository.getTgId();
            UserEntity base = (UserEntity) userDB.get(chatId);

            if (Utils.isOnlyNumbers(text)) {
                userDB.update("state", "none", chatId);
                userDB.update("proc", text, userId);

                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "setProfile/changed"));
                bot.execute(sm);
            } else {
                SendMessage sm = new SendMessage(chatId, manager.getText(base.getLanguage(), "nonNumber"));
                bot.execute(sm);
            }
        })));


        machine.addState(new State("getNewStopProfit:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) repo;
            String group = name.substring(name.indexOf(':') + 1).trim();
            UserEntity base = (UserEntity) userDB.get(chatId);

            try {
                if (Utils.isOnlyNumbers(message.getText())) {
                    service.setTPRationsByGroup(group, Arrays.stream(text.trim().replace(" ", "").split(",")).toList().stream().map(BigDecimal::new).toList());
                    SendMessage sm = new SendMessage(message.getChatId().toString(), manager.getText(base.getLanguage(), "setProfile/changed"));
                    bot.execute(sm);
                    userDB.update("state", "none", chatId);
                } else {
                    SendMessage sm = new SendMessage(message.getChatId().toString(), manager.getText(base.getLanguage(), "nonNumber"));
                    bot.execute(sm);
                }
            } catch (InvalidParametresException e) {
                SendMessage sm = new SendMessage(message.getChatId().toString(), "Сумма тейков должна быть 100%, повторите попытку:");
                bot.execute(sm);
            }
        })));


        machine.addState(new State("getProfitPercent:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) repo;
            String group = name.substring(name.indexOf(':') + 1).trim();
            try {
                if (Utils.isOnlyNumbers(text)) {
                    SendMessage sm = new SendMessage(chatId, "Введите процент переноса стопа:");
                    bot.execute(sm);
                    userDB.update("state", "getProfitStopPercent:"+group + "," + text, chatId);
                } else {
                    SendMessage sm = new SendMessage(message.getChatId().toString(), manager.getText(repository.getLanguage(), "nonNumber"));
                    bot.execute(sm);
                }
            } catch (InvalidParametresException e) {
                logger.error("ERROR: ", e);
            }
        })));


        machine.addState(new State("getProfitStopPercent:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) repo;
            String group = name.substring(name.indexOf(':') + 1).trim();

            String num = group.substring(group.indexOf(",")+1);
            group = group.substring(0, group.indexOf(','));
            try {
                BigDecimal stopPercent = new BigDecimal(text);
                service.setStopInProfitByGroup(group, num + "," + text, "xprofit");
                SendMessage sm = new SendMessage(chatId, manager.getText(repository.getLanguage(), "setProfile/changed"));
                bot.execute(sm);
                userDB.update("state", "none", chatId);
            } catch (InvalidParametresException e) {
                logger.error("ERROR: ", e);
                SendMessage sm = new SendMessage(message.getChatId().toString(), manager.getText(repository.getLanguage(), "nonNumber"));
                bot.execute(sm);
            }
        })));


        machine.addState(new State("getUIDsToAddUsersInGroup:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) repo;
            String group = name.substring(name.indexOf(':') + 1).trim();
            List<String> users = new ArrayList<>();
            try {
                if (text.contains(",")) {
                    users.addAll(Arrays.stream(text.split(",")).toList());
                } else users.add(text);
                logger.info("start updating");

                users.forEach(g -> {
                    Entity entity = userDB.getByUid(g);
                    if (entity != null) {
                        userDB.updateByUid("grp", group, g);
                    } else {
                        try {
                            bot.execute(new SendMessage(chatId, "Пользователь с UID: " + g + " не найден."));
                        } catch (TelegramApiException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                });
                userDB.update("state", "none", chatId);
            } catch (Exception e) {
                logger.error("Error error");
                SendMessage sm = new SendMessage(chatId, "Произошла ошибка во время выполнения операции");
                bot.execute(sm);
            }
        })));

        machine.addState(new State("getTakeNumber:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) repo;
            String group = name.substring(name.indexOf(':') + 1).trim();
            try {
                if (Utils.isOnlyNumbers(text)) {
                    SendMessage sm = new SendMessage(message.getChatId().toString(), "Введите процент для стопа");
                    bot.execute(sm);
                    userDB.update("state", "getTakePercent:" + group + "," + text, chatId);
                } else {
                    bot.execute(new SendMessage(chatId, "Неверный номер тейка, попробуйте еще раз."));
                }
            } catch (InvalidParametresException e) {
                logger.error("ERROR: ", e);
                SendMessage sm = new SendMessage(message.getChatId().toString(), manager.getText(repository.getLanguage(), "nonNumber"));
                bot.execute(sm);
            }
        })));

        machine.addState(new State("getTakePercent:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) repo;
            String group = name.substring(name.indexOf(':') + 1).trim();

            String num = group.substring(group.indexOf(",")+1);
            group = group.substring(0, group.indexOf(','));

            try {
                new BigDecimal(text);//проверяем что только цифры

                service.setStopInProfitByGroup(group, (num + "," + text), "take");
                SendMessage sm = new SendMessage(message.getChatId().toString(), manager.getText(repository.getLanguage(), "setProfile/changed"));
                bot.execute(sm);
                userDB.update("state", "none", chatId);
            } catch (InvalidParametresException e) {
                logger.error("ERROR: ", e);
                SendMessage sm = new SendMessage(message.getChatId().toString(), manager.getText(repository.getLanguage(), "nonNumber"));
                bot.execute(sm);
            }
        })));


        machine.addState(new State("getNewTakeProfit:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity repository = (UserEntity) repo;
            try {
                String group = name.substring(name.indexOf(':') + 1).trim();
                service.setTPRationsByGroup(group, Arrays.stream(text.trim().replace(" ", "").split(",")).toList().stream().map(BigDecimal::new).toList());
                SendMessage sm = new SendMessage(message.getChatId().toString(), manager.getText(repository.getLanguage(), "setProfile/changed"));
                bot.execute(sm);
                userDB.update("state", "none", chatId);
            } catch (InvalidParametresException e) {
                bot.execute(new SendMessage(chatId, "Сумма тейков должна быть 100%"));
                logger.error("ERROR: ", e);
            }
        })));


        machine.addState(new State("adminGetTemplNameForEditing", ((update, message, from, chatId, tmp, repo, StateName) -> {
            UserEntity adminRepo = (UserEntity) userDB.get(chatId);
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                if (templatesDB.getByKey("name", tmp).isEmpty()) {
                    bot.execute(new SendMessage(chatId, "Данного шаблона не существует, введите другой"));
                    return;
                }

                SendMessage edit = new SendMessage(chatId, "Выбирите, что изменить в шаблоне");
                Button name = new Button("Название", "SET_TEMPL_NAME:" + tmp);
                name.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    String tempName = text1.substring(text1.lastIndexOf(':') + 1).trim();
                    bot.execute(setMessage("Введите новое навзание шаблона:", message1));
                    userDB.update("state", "gettingTemplNName:" + tempName, chatId1);
                }));

                Button rus = new Button("Русский текст", "SET_TEMPL_RUS:" + tmp);
                rus.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    String tempName = text1.substring(text1.lastIndexOf(':') + 1).trim();
                    bot.execute(setMessage("Введите новый русский текст:", message1));
                    userDB.update("state", "gettingTemplNRu:" + tempName, chatId1);
                }));

                Button eng = new Button("Английский текст", "SET_TEMPL_ENG:" + tmp);
                eng.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    String tempName = text1.substring(text1.lastIndexOf(':') + 1).trim();
                    bot.execute(setMessage("Введите новый английский текст:", message1));
                    userDB.update("state", "gettingTemplNEn:" + tempName, chatId1);
                }));

                Button mdl = new Button("Молдавский текст", "SET_TEMPL_MDL:" + tmp);
                mdl.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    String tempName = text1.substring(text1.lastIndexOf(':') + 1).trim();
                    bot.execute(setMessage("Введите новый молдавский текст:", message1));
                    userDB.update("state", "gettingTemplNMd:" + tempName, chatId1);
                }));

                edit.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(name, rus), List.of(eng, mdl))));
                bot.execute(edit);
            } else sendNoPerms(bot, adminRepo);
            userDB.update("state", "none", chatId);
        })));
        machine.addState(new State("adminGetTemplNameForDeleting", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity adminRepo = (UserEntity) userDB.get(chatId);
            if (adminRepo.getRole().equals("admin")) {
                if (text.contains(",")) {
                    for (String n : text.split(",")) {
                        if (!templatesDB.getByKey("name", n.trim()).isEmpty()) {
                            templatesDB.remove("name", text.trim());
                        }
                        bot.execute(new SendMessage(chatId, "Шаблоны удалены."));
                    }
                } else {
                    if (!templatesDB.getByKey("name", text.trim()).isEmpty()) {
                        templatesDB.remove("name", text.trim());
                        bot.execute(new SendMessage(chatId, String.format("Шаблон %s удален.", text)));
                    }
                }
            } else sendNoPerms(bot, adminRepo);
            userDB.update("state", "none", chatId);
        })));
        machine.addState(new State("adminGetPairForCancelingLimits", ((update, message, from, chatId, pair, repo, name) -> {
            UserEntity adminRepo = (UserEntity) userDB.get(chatId);
            if (adminRepo.getRole().equals("admin")) {
                if (pair.contains(",")) {
                    for (String p : pair.split(",")) {
                        cancelLimit(p.trim(), chatId);
                    }
                } else cancelLimit(pair.trim(), chatId);
            } else sendNoPerms(bot, adminRepo);
            userDB.update("state", "none", chatId);
        })));
        machine.addState(new State("adminGetChanelNameForPositionsSource", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity adminRepo = (UserEntity) userDB.get(chatId);
            if (adminRepo.getRole().equals("admin")) {
                prefs.put("chanel-id", text);
                bot.execute(new SendMessage(chatId, "Канал изменен!"));
            } else sendNoPerms(bot, adminRepo);
            userDB.update("state", "none", chatId);
        })));
        machine.addState(new State("adminGetScalpChanelNameForPositionsSource", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity adminRepo = (UserEntity) userDB.get(chatId);
            if (adminRepo.getRole().equals("admin")) {
                prefs.put("tv-chanel-id", text);
                bot.execute(new SendMessage(chatId, "Канал по скальпу изменен!"));
            } else sendNoPerms(bot, adminRepo);
            userDB.update("state", "none", chatId);
        })));


        machine.addState(new State("admin_Get_TgId_For_Adding_New_Admin", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity adminRepo = (UserEntity) userDB.get(chatId);
            if (adminRepo.getRole().equals("admin")) {
                bot.execute(new SendMessage(chatId, "Введите роль которая будет у админа:"));
                userDB.update("state", "admin_Get_Role_For_Adding_New_Admin:" + text, chatId);
            } else sendNoPerms(bot, adminRepo);
        })));
        machine.addState(new State("admin_Get_Role_For_Adding_New_Admin:", ((update, message, from, chatId, text, repo, name) -> {
            UserEntity adminRepo = (UserEntity) userDB.get(chatId);
            UserEntity userRepo = (UserEntity) userDB.get(name.substring(name.lastIndexOf(':') + 1));
            if (adminRepo.getRole().equals("admin")) {
                String id = userRepo.getTgId();
                String userName = userRepo.getTgName();
                userDB.update("role", text.toLowerCase(), id);
                bot.execute(new SendMessage(chatId, userName + " добавлен как админ, с ролью: " + text + "."));
            } else sendNoPerms(bot, adminRepo);
            userDB.update("state", "none", chatId);
        })));

        machine.addState(new State("admin_Get_TgId_For_Deleting_Admin", ((update, message, from, chatId, id, repo, name) -> {
            UserEntity adminRepo = (UserEntity) userDB.get(chatId);
            if (adminRepo.getRole().equals("admin")) {
                if (id.contains(",")) {
                    for (String p : id.split(",")) {
                        p = p.trim();
                        String userName = ((UserEntity) userDB.get(p)).getTgName();
                        userDB.update("role", "user", p);
                        bot.execute(new SendMessage(chatId, userName + " был удален из админов."));
                    }
                } else {
                    String userName = ((UserEntity) userDB.get(id)).getTgName();
                    userDB.update("role", "user", id);
                    bot.execute(new SendMessage(chatId, userName + " был удален из админов."));
                }
            } else sendNoPerms(bot, adminRepo);
            userDB.update("state", "none", chatId);
        })));
        machine.addState(new State("admin_Get_TgId_For_Deleting_User", ((update, message, from, chatId, id, repo, name) -> {
            UserEntity adminRepo = (UserEntity) userDB.get(chatId);
            if (adminRepo.getRole().equals("admin")) {
                if (id.contains(",")) {
                    for (String p : id.split(",")) {
                        p = p.trim();
                        String userName = ((UserEntity) userDB.get(p)).getTgName();
                        userDB.remove(p);
                        bot.execute(new SendMessage(chatId, userName + " был удален из бота."));
                    }
                } else {
                    String userName = ((UserEntity) userDB.get(id)).getTgName();
                    userDB.remove(id);
                    bot.execute(new SendMessage(chatId, userName + " был удален из бота."));
                }
            } else sendNoPerms(bot, adminRepo);
            userDB.update("state", "none", chatId);
        })));
    }

    private InlineKeyboardMarkup getMarkup(String pos, String lang, String userId) {
        Button cancel = new Button(manager.getText(lang, "cancelAllPos"), "ACC_ALL_POS");
        Button accept = new Button(manager.getText(lang, "acceptAllPos"), "CANCEL_ALL_POS");

        cancel.setActionListener(((update, msg, from, chatId, text, repository) -> {
            Button all = new Button(manager.getText(lang, "allPos"), "ALL");
            all.setActionListener(((update1, message, from1, chatId1, text1, repository1) -> {
                EditMessageText et = new EditMessageText(manager.getText(lang, "onlyExpTraders"));
                et.setReplyMarkup(getMarkup(pos, lang, userId));
                et.setChatId(chatId1);
                et.setMessageId(message.getMessageId());
                bot.execute(et);
            }));


            EditMessageText txt = new EditMessageText(pos);
            txt.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(all))));
            txt.setParseMode("HTML");
            txt.setChatId(from.getId().toString());

            txt.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

            userDB.update("state", "getPositions:" + userId, from.getId().toString());

            bot.execute(txt);
        }));

        accept.setActionListener(((update, msg, from, chatId, text, repository) -> {
            SendMessage message = new SendMessage(from.getId().toString(), manager.getText(lang, "getPlecho"));
            message.setParseMode("HTML");
            bot.execute(message);

            EditMessageText txt = new EditMessageText(manager.getText(lang, "onlyExpTradersNext"));
            txt.setParseMode("HTML");
            txt.setChatId(from.getId().toString());
            txt.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

            userDB.update("state", "getPlecho:" + userId, from.getId().toString());
            userDB.update("positions", "all", userId);

            bot.execute(txt);
        }));

        return new InlineKeyboardMarkup(List.of(List.of(accept, cancel)));
    }

    private EditMessageText setMessage(String text, Message m) {
        EditMessageText e = new EditMessageText(text);
        e.setChatId(m.getChatId());
        e.setMessageId(m.getMessageId());
        return e;
    }

    private void cancelLimit(String pair, String chatId) {
        new Thread(() -> {
            try {
                for (UserEntity entity : userDB.getAll()) {
                    Thread.startVirtualThread(() -> {
                        try {
                            TradeService ts = entity.getUserBeerj();
                            List<String> ids = new ArrayList<>();
                            ts.getOrders(entity).stream().filter(o -> {
                                String symbol = o.getSymbol();
                                return symbol.equalsIgnoreCase(pair) && o.getTradeSide().equalsIgnoreCase("close");
                            }).forEach(order -> ids.add(order.getOrderId()));
                            ids.forEach(s -> {
                                Order order = new Order();
                                order.setOrderId(s);
                                order.setSymbol(pair);
                                OrderResult e = ts.closeOrder(entity, order);
                                if (e.succes()) {
                                    logger.info("Limit canceled");
                                }
                            });
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    });
                }
                bot.execute(new SendMessage(chatId, "Лимитные оредра успешно отменены"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }).start();
    }

    private void sendGetUid(Message message, String lang, User from, String who) throws Exception {
        EditMessageText et = new EditMessageText();
        et.setParseMode("HTML");
        et.setMessageId(message.getMessageId());
        et.setChatId(message.getChatId());
        et.setText(manager.getText(lang, "lcsacc"));
        userDB.update("state", "getUid:" + who, from.getId().toString());
        bot.execute(et);
    }
}
