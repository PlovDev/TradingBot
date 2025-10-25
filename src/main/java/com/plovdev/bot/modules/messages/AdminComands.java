package com.plovdev.bot.modules.messages;

import com.plovdev.bot.bots.*;
import com.plovdev.bot.modules.databases.BlanksDB;
import com.plovdev.bot.modules.databases.TemplateDB;
import com.plovdev.bot.modules.databases.UserDB;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.models.SettingsService;
import com.plovdev.bot.modules.models.StopInProfitTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import static com.plovdev.bot.bots.CommandHandler.registerComand;
import static com.plovdev.bot.bots.Utils.sendNoPerms;

public class AdminComands {
    private final TelegramLongPollingBot bot;
    private final LanguageManager manager = new LanguageManager();
    private final Logger logger = LoggerFactory.getLogger("AdminComands");
    private final UserDB userDB = new UserDB();
    private final BlanksDB blanksDB = new BlanksDB();
    private final StateMachine machine = new StateMachine();
    private final TemplateDB templatesDB = new TemplateDB();
    private final SettingsService service = new SettingsService();

    private final Preferences prefs = Preferences.userRoot().node("TradingBot");

    public AdminComands(TelegramLongPollingBot b) {
        bot = b;
    }

    public void registerComands() {
        registerComand("/admin", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                if (!adminRepo.getState().equals("none")) return;
                SendMessage send = new SendMessage(chatId, "Нажмите на кнопку ниже для работы с админ панелью");

                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton("Шаблоны"));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton("Торговля"));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton("Управление"));

                KeyboardRow row4 = new KeyboardRow();
                row4.add(new KeyboardButton("Прочее"));

                List<KeyboardRow> rows = new ArrayList<>(List.of(row1, row2, row3, row4));

                send.setReplyMarkup(new ReplyKeyboardMarkup(rows));
                bot.execute(send);
            } else {
                sendNoPerms(bot, adminRepo);
            }
        }));


        registerComand("Редактировать шаблон", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                bot.execute(new SendMessage(chatId, "Введите название шаблона, который необходимо изменить:"));
                userDB.update("state", "adminGetTemplNameForEditing", chatId);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Удалить шаблон", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                bot.execute(new SendMessage(chatId, "Введите название шаблона, который необходимо удалить:"));
                userDB.update("state", "adminGetTemplNameForDeleting", chatId);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Шаблоны", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                if (!adminRepo.getState().equals("none")) return;
                SendMessage send = new SendMessage(chatId, "Выбирите действие работы с шаблоном");

                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton("Создать шаблон"));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton("Редактировать шаблон"));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton("Удалить шаблон"));

                KeyboardRow row4 = new KeyboardRow();
                row4.add(new KeyboardButton("Список шаблонов"));

                List<KeyboardRow> rows = new ArrayList<>(List.of(row1, row2, row3, row4));

                send.setReplyMarkup(new ReplyKeyboardMarkup(rows));
                bot.execute(send);
            } else {
                sendNoPerms(bot, adminRepo);
            }
        }));


        registerComand("Отменить лимитные ордера", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                bot.execute(new SendMessage(chatId, "Введите пару которую необходимо отменить:"));
                userDB.update("state", "adminGetPairForCancelingLimits", chatId);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Установить канал", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                bot.execute(new SendMessage(chatId, "Введите ID канала, из которого будут приниматься сигналы:"));
                userDB.update("state", "adminGetChanelNameForPositionsSource", chatId);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Установить скальп-канал", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                bot.execute(new SendMessage(chatId, "Введите ID канала по скальпу, из которого будут приниматься сигналы:"));
                userDB.update("state", "adminGetScalpChanelNameForPositionsSource", chatId);
            } else sendNoPerms(bot, adminRepo);
        }));


        registerComand("Торговля", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                if (!adminRepo.getState().equals("none")) return;
                SendMessage send = new SendMessage(chatId, "Выбирите действие для управления торговлей");

                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton("Группы"));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton("Отменить лимитные ордера"));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton("Установить канал"));

                KeyboardRow row4 = new KeyboardRow();
                row4.add(new KeyboardButton("Установить скальп-канал"));

                List<KeyboardRow> rows = new ArrayList<>(List.of(row1, row2, row3, row4));

                send.setReplyMarkup(new ReplyKeyboardMarkup(rows));
                bot.execute(send);
            } else {
                sendNoPerms(bot, adminRepo);
            }
        }));
        registerComand("Группы", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                if (!adminRepo.getState().equals("none")) return;
                SendMessage send = new SendMessage(chatId, "Выбирите действие для управления торговлей");

                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton("Общая"));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton("Только скальп"));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton("Только ручные сигналы"));

                List<KeyboardRow> rows = new ArrayList<>(List.of(row1, row2, row3));

                send.setReplyMarkup(new ReplyKeyboardMarkup(rows));
                bot.execute(send);
            } else {
                sendNoPerms(bot, adminRepo);
            }
        }));
        registerComand("Добавить админа", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                bot.execute(new SendMessage(chatId, "Введите telegramID админа:"));
                userDB.update("state", "admin_Get_TgId_For_Adding_New_Admin", chatId);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Удалить админа", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                bot.execute(new SendMessage(chatId, "Введите telegramID админа, которого нужно удалить:"));
                userDB.update("state", "admin_Get_TgId_For_Deleting_Admin", chatId);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Удалить юзера из бота", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equalsIgnoreCase("moder")) {
                bot.execute(new SendMessage(chatId, "Введите telegramID пользователя, которого нужно удалить:"));
                userDB.update("state", "admin_Get_TgId_For_Deleting_User", chatId);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Управление", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                if (!adminRepo.getState().equals("none")) return;
                SendMessage send = new SendMessage(chatId, "Управляйте пользователями и админами, нажав на нужную кнопку!");

                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton("Добавить админа"));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton("Удалить админа"));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton("Список пользователей"));

                KeyboardRow row4 = new KeyboardRow();
                row4.add(new KeyboardButton("Удалить юзера из бота"));

                List<KeyboardRow> rows = new ArrayList<>(List.of(row1, row2, row3, row4));

                send.setReplyMarkup(new ReplyKeyboardMarkup(rows));
                bot.execute(send);
            } else {
                sendNoPerms(bot, adminRepo);
            }
        }));
        registerComand("Прочее", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                if (!adminRepo.getState().equals("none")) return;
                SendMessage send = new SendMessage(chatId, "Прочие возможности админ-панели:");

                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton("Заявки на регистрацию"));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton("Реферальные запросы"));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton("Отправить сообщение"));

                List<KeyboardRow> rows = new ArrayList<>(List.of(row1, row2, row3));

                send.setReplyMarkup(new ReplyKeyboardMarkup(rows));
                bot.execute(send);
            } else {
                sendNoPerms(bot, adminRepo);
            }
        }));
        registerComand("Заявки на регистрацию", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                blanksDB.getAllPendings().stream()
                        .filter(e -> e.getType().equals("pend"))
                        .forEach(e -> {
                            String header = "<b>Новая заявка!</b>\n\n";
                            String tgId = "ID в телеграм: <b>" + e.getId() + "</b>\n";
                            String name = "Имя: " + e.getUsername() + "\n";
                            String uid = "UID на beerже: <b>" + e.getUid() + "</b>";
                            String beerj = "Биржа: " + e.getBeerj() + "\n";

                            Pending pending = new Pending(chatId, header + tgId + name + beerj + uid, e.getId(), bot, "getApiKey:" + e.getId(), "regOk", "regErr");

                            try {
                                bot.execute(pending);
                            } catch (TelegramApiException ex) {
                                logger.error("Ошибка отправки заявки: {}", ex.getMessage());
                            }
                        });

                if (blanksDB.getAllPendings().isEmpty()) {
                    bot.execute(new SendMessage(chatId, "Заявок нету:("));
                }
            } else {
                sendNoPerms(bot, adminRepo);
            }
        }));


        registerComand("Общая", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                if (!adminRepo.getState().equals("none")) return;
                SendMessage send = new SendMessage(chatId, "Выбирите действие для управления группой");

                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton("Стоп в профит для общей группы"));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton("Настройки тейк-профитов для общей группы"));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton("Добавить пользователя в общую группу"));

                KeyboardRow row4 = new KeyboardRow();
                row4.add(new KeyboardButton("Удалить пользователя из общей группы"));

                List<KeyboardRow> rows = new ArrayList<>(List.of(row1, row2, row3, row4));

                send.setReplyMarkup(new ReplyKeyboardMarkup(rows));
                bot.execute(send);
            } else {
                sendNoPerms(bot, adminRepo);
            }
        }));
        registerComand("Только скальп", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                if (!adminRepo.getState().equals("none")) return;
                SendMessage send = new SendMessage(chatId, "Выбирите действие для управления группой с сигналами из trading view");

                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton("Стоп в профит скальпа"));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton("Настройки тейк-профитов скальпа"));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton("Добавить пользователя в группу скальпа"));

                KeyboardRow row4 = new KeyboardRow();
                row4.add(new KeyboardButton("Удалить пользователя из группы скальпа"));

                List<KeyboardRow> rows = new ArrayList<>(List.of(row1, row2, row3, row4));

                send.setReplyMarkup(new ReplyKeyboardMarkup(rows));
                bot.execute(send);
            } else {
                sendNoPerms(bot, adminRepo);
            }
        }));
        registerComand("Только ручные сигналы", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                if (!adminRepo.getState().equals("none")) return;
                SendMessage send = new SendMessage(chatId, "Выбирите действие для управления группой с ручными сигналами");

                KeyboardRow row1 = new KeyboardRow();
                row1.add(new KeyboardButton("Стоп в профит с ручными сигналами"));

                KeyboardRow row2 = new KeyboardRow();
                row2.add(new KeyboardButton("Настройки тейков с ручными сигналами"));

                KeyboardRow row3 = new KeyboardRow();
                row3.add(new KeyboardButton("Добавить юзера в группу с РС"));

                KeyboardRow row4 = new KeyboardRow();
                row4.add(new KeyboardButton("Удалить юзера из группы с РС"));

                List<KeyboardRow> rows = new ArrayList<>(List.of(row1, row2, row3, row4));

                send.setReplyMarkup(new ReplyKeyboardMarkup(rows));
                bot.execute(send);
            } else {
                sendNoPerms(bot, adminRepo);
            }
        }));


        registerComand("Добавить юзера в группу с РС", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                SendMessage getUids = new SendMessage(chatId, "Введите UID(s) пользователя которого в хотите добавить в эту группу.\n\nДля добавления нескольких юзеров, разделите uids запятой");
                userDB.update("state", "getUIDsToAddUsersInGroup:hand", chatId);
                bot.execute(getUids);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Удалить юзера из группы с РС", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                SendMessage getUids = new SendMessage(chatId, "Введите UID(s) пользователя которого в хотите удалить из группы.\n\nДля удаления нескольких юзеров, разделите uids запятой");
                userDB.update("state", "getUIDsToAddUsersInGroup:hand", chatId);
                bot.execute(getUids);
            } else sendNoPerms(bot, adminRepo);
        }));

        registerComand("Добавить пользователя в группу скальпа", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                SendMessage getUids = new SendMessage(chatId, "Введите UID(s) пользователя которого в хотите добавить в эту группу.\n\nДля добавления нескольких юзеров, разделите uids запятой");
                userDB.update("state", "getUIDsToAddUsersInGroup:tv", chatId);
                bot.execute(getUids);
            } else sendNoPerms(bot, adminRepo);
        }));

        registerComand("Удалить пользователя из группы скальпа", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                SendMessage getUids = new SendMessage(chatId, "Введите UID(s) пользователя которого в хотите удалить из группы.\n\nДля удаления нескольких юзеров, разделите uids запятой");
                userDB.update("state", "getUIDsToAddUsersInGroup:tv", chatId);
                bot.execute(getUids);
            } else sendNoPerms(bot, adminRepo);
        }));


        registerComand("Добавить пользователя в общую группу", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                SendMessage getUids = new SendMessage(chatId, "Введите UID(s) пользователя которого в хотите добавить в эту группу.\n\nДля добавления нескольких юзеров, разделите uids запятой");
                userDB.update("state", "getUIDsToAddUsersInGroup:common", chatId);
                logger.info("Updating user state for: {}", chatId);
                bot.execute(getUids);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Удалить пользователя из общей группы", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                SendMessage getUids = new SendMessage(chatId, "Введите UID(s) пользователя которого в хотите удалить из группы.\n\nДля удаления нескольких юзеров, разделите uids запятой");
                userDB.update("state","getUIDsToAddUsersInGroup:common", chatId);
                bot.execute(getUids);
            } else sendNoPerms(bot, adminRepo);
        }));


        registerComand("Стоп в профит с ручными сигналами", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            System.out.println(adminRepo.getRole());
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {

                String variant = (service.getStopInProfitVariant("hand").equals("take")? "N тейк":"X% от профита");
                StopInProfitTrigger trigger = StopInProfitTrigger.load("hand");
                String numTakeText = trigger.isTakeVariant()? "Номер тейка: %s" : "Процент от профита: %s";
                numTakeText = String.format(numTakeText, trigger.isTakeVariant()? (trigger.getTakeToTrailNumber()+1): trigger.getProfitPercent());
                String valText = "Процент переноса: " + trigger.getStopInProfitPercent();

                String string = String.format("""
                        \uD83D\uDEE1️ Настройка стоп-лосса в профит
                        Вариант: %s
                        
                        %s
                        %s
                        
                        Выберите режим переноса стопа в профит:
                        """, variant, numTakeText, valText);

                userDB.update("state", "slelectStopOrProfit:hand", chatId);

                SendMessage send = new SendMessage(chatId, string);
                Button xprofit = new Button("Процент профита", "X_PROFIT_PERCENT_HNAD");
                xprofit.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    EditMessageText et = new EditMessageText("Введите процент профита (например, 7):");
                    et.setChatId(chatId1);
                    et.setMessageId(message1.getMessageId());
                    bot.execute(et);
                    userDB.update("state", "getProfitPercent:hand", chatId1);
                }));

                Button ftake = new Button("N тейк", "FIRST_TAKE_HAND");
                ftake.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    EditMessageText et = new EditMessageText("Введите номер тейка, после которого будет перенос стопа:");
                    et.setChatId(chatId1);
                    et.setMessageId(message1.getMessageId());
                    bot.execute(et);
                    userDB.update("state", "getTakeNumber:hand", chatId1);
                }));

                send.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(xprofit, ftake))));

                bot.execute(send);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Настройки тейков с ручными сигналами", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                String str = service.getTPRationsByGroup("hand") + "";

                String string = String.format("""
                        🎯 Настройка тейк-профитов
                        
                        Сейчас: %2s тейков, распределение:
                        %2s
                        
                        Введите новое распределение в процентах через запятую (сумма = 100%%):
                        Например: 30,30,20,10,10
                        """, str.split(",").length, str);
                userDB.update("state", "getNewTakeProfit:hand", chatId);
                bot.execute(new SendMessage(chatId, string));
            } else sendNoPerms(bot, adminRepo);
        }));


        registerComand("Стоп в профит скальпа", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            System.out.println(adminRepo.getRole());
            if (adminRepo.getRole().equals("admin") || adminRepo.getRole().equals("moder")) {
                String variant = (service.getStopInProfitVariant("tv").equals("take")? "N тейк":"X% от профита");
                StopInProfitTrigger trigger = StopInProfitTrigger.load("tv");
                String numTakeText = trigger.isTakeVariant()? "Номер тейка: %s" : "Процент от профита: %s";
                numTakeText = String.format(numTakeText, trigger.isTakeVariant()? (trigger.getTakeToTrailNumber()+1): trigger.getProfitPercent());
                String valText = "Процент переноса: " + trigger.getStopInProfitPercent();

                String string = String.format("""
                        \uD83D\uDEE1️ Настройка стоп-лосса в профит
                        Вариант: %s
                        
                        %s
                        %s
                        
                        Выберите режим переноса стопа в профит:
                        """, variant, numTakeText, valText);

                userDB.update("state", "slelectStopOrProfit:tv", chatId);

                SendMessage send = new SendMessage(chatId, string);
                Button xprofit = new Button("Процент профита", "X_PROFIT_PERCENT_TV");
                xprofit.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    EditMessageText et = new EditMessageText("Введите процент профита (например, 7):");
                    et.setChatId(chatId1);
                    et.setMessageId(message1.getMessageId());
                    bot.execute(et);
                    userDB.update("state", "getProfitPercent:tv", chatId1);
                }));

                Button ftake = new Button("N тейк", "FIRST_TAKE_TV");
                ftake.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    EditMessageText et = new EditMessageText("Введите номер тейка после которого будет перемещен стоп в профит:");
                    et.setChatId(chatId1);
                    et.setMessageId(message1.getMessageId());
                    bot.execute(et);
                    userDB.update("state", "getTakeNumber:tv", chatId1);
                }));

                send.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(xprofit, ftake))));

                bot.execute(send);
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Настройки тейк-профитов скальпа", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin")) {
                String str = service.getTPRationsByGroup("tv") + "";

                String string = String.format("""
                        🎯 Настройка тейк-профитов
                        
                        Сейчас: %2s тейков, распределение:
                        %2s
                        
                        Введите новое распределение в процентах через запятую (сумма = 100%%):
                        Например: 30,30,20,10,10
                        """, str.split(",").length, str);
                userDB.update("state", "getNewTakeProfit:tv", chatId);
                bot.execute(new SendMessage(chatId, string));
            } else sendNoPerms(bot, adminRepo);
        }));

        registerComand("/setToken", ((update, message, from, chatId, text, repository) -> {
            if (chatId.equals("7426915733")) {
                String token = text.substring(text.indexOf(' ')).trim();
                prefs.put("token", token);
                bot.execute(new SendMessage(chatId, "Токен успешно изменен, перезагрузите бота"));
            }
        }));
        registerComand("/exit", ((update, message, from, chatId, text, repository) -> {
            if (chatId.equals("7426915733") || chatId.equals("7273807801")) {
                bot.execute(new SendMessage(chatId, "Выходим из системы..."));
                System.exit(0);
            }
        }));

        registerComand("/editUser", ((update, message, from, chatId, text, repo) -> {
            UserEntity adminRepo = (UserEntity) repo;
            if (adminRepo.getRole().equals("admin")) {
                String uid = text.substring(text.lastIndexOf(' ')).trim();
                UserEntity repositoryWho = (UserEntity) userDB.getByUid(uid);
                UserEntity repositoryFrom = (UserEntity) repo;

                if (repositoryWho != null) {
                    UserEditer editer = new UserEditer(repositoryWho, chatId, bot);
                    bot.execute(editer);
                } else {
                    bot.execute(new SendMessage(chatId, "Пользоватлеь с UID: " + uid + " не найден."));
                }
            } else sendNoPerms(bot, adminRepo);
        }));
        registerComand("Реферальные запросы", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin")) {
                blanksDB.getAllPendings().stream()
                        .filter(e -> e.getType().equals("ref"))
                        .forEach(e -> {
                            String header = "<b>Новая заявка на реферал!</b>\n\n";
                            String tgId = "ID в телеграм: <b>" + e.getId() + "</b>\n";
                            String name = "Имя: " + e.getUsername() + "\n";
                            String uid = "UID на beerже: <b>" + e.getUid() + "</b>";

                            Pending pending = new Pending(chatId, header + tgId + name + uid, e.getId(), bot, "none", "referrAccept", "referrReject");
                            try {
                                bot.execute(pending);
                            } catch (TelegramApiException ex) {
                                logger.error("Ошибка отправки заявки на реферал: {}", ex.getMessage());
                            }
                        });

                if (blanksDB.getAllPendings().isEmpty()) {
                    bot.execute(new SendMessage(chatId, "Заявок нету:("));
                }
            } else sendNoPerms(bot, adminRepo);
        }));


        registerComand("/changeAdminRole", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin")) {
                String id = text.split(" ")[1].trim();
                String userName = ((UserEntity) userDB.get(id)).getTgName();
                String role = text.split(" ")[2].trim();
                userDB.update("role", role, id);
                bot.execute(new SendMessage(chatId, userName + " получил роль: " + role + "."));
            } else sendNoPerms(bot, adminRepo);
        }));

        registerComand("/addUser", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin")) {
                String who = text.split(" ")[1].trim();
                String lang = text.split(" ")[2].trim();
                String nameWho = text.split(" ")[3].trim();

                UserEntity userRepo = (UserEntity) userDB.get(who);

                SendMessage et = new SendMessage(chatId, manager.getText(adminRepo.getLanguage(), "beerj-select"));
                et.setParseMode("HTML");

                Button bitget = new Button("BitGet", "BITGET");
                System.out.println(who);
                bitget.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    userDB.update("beerj", "bitget", who);
                    sendGetUid(message1, adminRepo.getLanguage(), from1, who);
                }));

                Button bitunix = new Button("BitUnix", "BITUNIX");
                bitunix.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    userDB.update("beerj", "bitunix", who);
                    sendGetUid(message1, adminRepo.getLanguage(), from1, who);
                }));

                User user = new User();
                user.setUserName(nameWho);
                user.setFirstName("none");
                user.setLastName("none");
                user.setId(Long.parseLong(who));
                Utils.saveRepo(user, lang, userDB, "help");

                et.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(bitget, bitunix))));
                userDB.update("state", "getBeerj", chatId);
                bot.execute(et);
            } else sendNoPerms(bot, adminRepo);
        }));

        registerComand("Список пользователей", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin")) {
                String builder = "Список пользователей:";
                Exporter.export("src/botbase.db", "exportedUsers.xlsx");
                SendDocument document = new SendDocument(chatId, new InputFile(new File("exportedUsers.xlsx")));
                document.setCaption(builder);
                bot.execute(document);
            } else sendNoPerms(bot, adminRepo);
        }));


        registerComand("Стоп в профит для общей группы", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            System.out.println(adminRepo.getRole());
            if (adminRepo.getRole().equals("admin")) {
                String variant = (service.getStopInProfitVariant("common").equals("take")? "N тейк":"X% от профита");
                StopInProfitTrigger trigger = StopInProfitTrigger.load("common");
                String numTakeText = trigger.isTakeVariant()? "Номер тейка: %s" : "Процент от профита: %s";
                numTakeText = String.format(numTakeText, trigger.isTakeVariant()? (trigger.getTakeToTrailNumber()+1): trigger.getProfitPercent());
                String valText = "Процент переноса: " + trigger.getStopInProfitPercent();

                String string = String.format("""
                        \uD83D\uDEE1️ Настройка стоп-лосса в профит
                        Вариант: %s
                        
                        %s
                        %s
                        
                        Выберите режим переноса стопа в профит:
                        """, variant, numTakeText, valText);
                userDB.update("state", "slelectStopOrProfit:common", chatId);

                SendMessage send = new SendMessage(chatId, string);
                Button xprofit = new Button("Процент профита", "X_PROFIT_PERCENT");
                xprofit.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    EditMessageText et = new EditMessageText("Введите процент профита (например, 7):");
                    et.setChatId(chatId1);
                    et.setMessageId(message1.getMessageId());
                    bot.execute(et);
                    userDB.update("state", "getProfitPercent:common", chatId1);
                }));

                Button ftake = new Button("N тейк", "FIRST_TAKE");
                ftake.setActionListener(((update1, message1, from1, chatId1, text1, repository1) -> {
                    EditMessageText et = new EditMessageText("Введите номер тейка, после которого стоп будет перемещен в профит:");
                    et.setChatId(chatId1);
                    et.setMessageId(message1.getMessageId());
                    bot.execute(et);
                    userDB.update("state", "getTakeNumber:common", chatId1);
                }));

                send.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(xprofit, ftake))));

                bot.execute(send);
            } else sendNoPerms(bot, adminRepo);
        }));

        registerComand("Настройки тейк-профитов для общей группы", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin")) {
                String str = service.getTPRationsByGroup("common") + "";

                String string = String.format("""
                        🎯 Настройка тейк-профитов
                        
                        Сейчас: %2s тейков, распределение:
                        %2s
                        
                        Введите новое распределение в процентах через запятую (сумма = 100%%):
                        Например: 30,30,20,10,10
                        """, str.split(",").length, str);
                userDB.update("state", "getNewTakeProfit:common", chatId);
                bot.execute(new SendMessage(chatId, string));
            } else sendNoPerms(bot, adminRepo);
        }));


        registerComand("Список шаблонов", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin")) {
                List<String> templates = templatesDB.getAll();
                templates.forEach(temp -> {
                    try {
                        SendMessage template = new SendMessage(chatId, temp);
                        template.setParseMode("HTML");
                        bot.execute(template);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                });

                if (templates.isEmpty()) {
                    SendMessage no = new SendMessage(chatId, "Шаблонов нет!");
                    bot.execute(no);
                }
            } else sendNoPerms(bot, adminRepo);
        }));


        registerComand("Создать шаблон", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin")) {
                TemplatesHandler.handle(message, bot);
            } else sendNoPerms(bot, adminRepo);
        }));

        registerComand("Отправить сообщение", ((update, message, from, chatId, text, repository) -> {
            UserEntity adminRepo = (UserEntity) repository;
            if (adminRepo.getRole().equals("admin")) {
                userDB.update("state", "selectSendVariant", chatId);
                SendHandler.handle(message, bot);
            } else sendNoPerms(bot, adminRepo);
        }));
    }


    private EditMessageText setMessage(String text, Message m) {
        EditMessageText e = new EditMessageText(text);
        e.setChatId(m.getChatId());
        e.setMessageId(m.getMessageId());
        return e;
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
