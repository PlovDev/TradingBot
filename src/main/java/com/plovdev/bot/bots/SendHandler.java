package com.plovdev.bot.bots;

import com.plovdev.bot.modules.databases.TemplateDB;
import com.plovdev.bot.modules.databases.UserDB;
import com.plovdev.bot.modules.databases.UserEntity;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public class SendHandler {
    private static final UserDB adminsDB = new UserDB();
    private static final TemplateDB templatesDB = new TemplateDB();
    private static final LanguageManager manager = new LanguageManager();

    private static String uids = "";
    private static boolean isUid = false;

    public static void handle(Message input, TelegramLongPollingBot bot) throws Exception {
        User user = input.getFrom();
        String userId = user.getId().toString();
        UserEntity repository = (UserEntity) adminsDB.get(userId);

        String state = repository.getState();

        Button button1 = new Button("Всем", "SALL", ((update, message, from, chatId, text, repository1) -> {
            SendHandler.setVariants(message, bot, "SALL");
        }));
        Button button2 = new Button("По UID", "SIUD", ((update, message, from, chatId, text, repository1) -> {
            SendHandler.setVariants(message, bot, "SUID");
        }));

        if (!state.equals("none") && input.getText().startsWith("/")) {
            if (input.getText().startsWith("/cancel")) {
                adminsDB.update("state", "none", userId);
                bot.execute(new SendMessage(userId, "Отменено"));
            }
            return;
        }

        switch (state) {
            case "selectSendVariant" -> {
                SendMessage message = new SendMessage(userId, "Выбирите способ отправки:");
                message.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(button1, button2))));
                bot.execute(message);
            }
            case "gettingSendInputTempl" -> {
                sendTemplate(input.getText(), input.getChatId().toString(), input, bot);
                adminsDB.update("state", "none", userId);
            }
            case "gettingSendInputText" -> {
                sendText(input.getText(), input.getChatId().toString(), input, bot);
                adminsDB.update("state", "none", userId);
            }
            case "gettingSendInputUid" -> {
                uids = input.getText().trim();

                SendMessage sall = new SendMessage(input.getChatId().toString(), "Отправить шаблоном или текстом?");
                sendEndVar(input, bot);
            }
        }
    }

    public static void setVariants(Message inputMessage, TelegramLongPollingBot bot, String data) throws Exception {
        String chatId = inputMessage.getChatId().toString();
        Integer mId = inputMessage.getMessageId();

        switch (data) {
            case "SALL" -> setEndVar(inputMessage, bot);
            case "TEMPLOM" -> {
                adminsDB.update("state", "gettingSendInputTempl", chatId);
                EditMessageText temp = new EditMessageText("Введите название шаблона");
                temp.setChatId(chatId);
                temp.setMessageId(inputMessage.getMessageId());
                bot.execute(temp);
            }
            case "TEXTOM" -> {
                adminsDB.update("state", "gettingSendInputText", chatId);
                EditMessageText temp = new EditMessageText("Введите текст сообщения");
                temp.setChatId(chatId);
                temp.setMessageId(inputMessage.getMessageId());
                bot.execute(temp);
            }
            case "SUID" -> {
                adminsDB.update("state", "gettingSendInputUid", chatId);

                isUid = true;

                EditMessageText temp = new EditMessageText("Введите UID кому отправить сообщение.");
                temp.setChatId(chatId);
                temp.setMessageId(inputMessage.getMessageId());
                bot.execute(temp);
            }
        }
    }

    public static void setEndVar(Message inputMessage, TelegramLongPollingBot bot) throws Exception {
        String chatId = inputMessage.getChatId().toString();
        Integer mId = inputMessage.getMessageId();

        EditMessageText sall = new EditMessageText("Отправить шаблоном или текстом?");

        Button button1 = new Button("Шаблоном", "TEMPLOM", ((update, message, from, chatId1, text, repository1) -> {
            SendHandler.setVariants(message, bot, "TEMPLOM");
        }));
        Button button2 = new Button("Текстом", "TEXTOM", ((update, message, from, chatId1, text, repository1) -> {
            SendHandler.setVariants(message, bot, "TEXTOM");
        }));


        sall.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(button1, button2))));


        sall.setChatId(chatId);
        sall.setMessageId(mId);
        bot.execute(sall);
    }

    public static void sendEndVar(Message inputMessage, TelegramLongPollingBot bot) throws Exception {
        String chatId = inputMessage.getChatId().toString();
        Integer mId = inputMessage.getMessageId();

        SendMessage sall = new SendMessage(inputMessage.getChatId().toString(), "Отправить шаблоном или текстом?");

        Button button1 = new Button("Шаблоном", "TEMPLOM", ((update, message, from, chatId1, text, repository1) -> {
            SendHandler.setVariants(message, bot, "TEMPLOM");
        }));
        Button button2 = new Button("Текстом", "TEXTOM", ((update, message, from, chatId1, text, repository1) -> {
            SendHandler.setVariants(message, bot, "TEXTOM");
        }));


        sall.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(button1, button2))));

        bot.execute(sall);
    }

    private static void sendText(String text, String msg, Message m, TelegramLongPollingBot bot) throws Exception {
        if (isUid) {
            for (String chatId : uids.replace(" ", "").split(",")) {
                try {
                    UserEntity user = (UserEntity) adminsDB.getByUid(chatId);
                    String lang = user.getLanguage();
                    SendMessage send = new SendMessage(user.getTgId(), manager.getText(lang, "admmsg") + "\n\n\"" + text + "\"");
                    send.setParseMode("HTML");
                    bot.execute(send);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } else {
            adminsDB.getAll().forEach(e -> {
                try {
                    String chatId = e.getTgId();
                    if (!chatId.equals(msg)) {
                        String lang = e.getLanguage();
                        SendMessage send = new SendMessage(chatId, manager.getText(lang, "admmsg") + "\n\n\"" + text + "\"");
                        send.setParseMode("HTML");
                        bot.execute(send);
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            });
        }
        adminsDB.update("state", "none", m.getFrom().getId().toString());
        bot.execute(new SendMessage(msg, "Сообщения доставлены!"));
        isUid = false;
    }

    private static void sendTemplate(String message, String cId, Message m, TelegramLongPollingBot bot) throws Exception {
        if (isUid) {
            for (String chatId : uids.replace(" ", "").split(",")) {
                try {
                    UserEntity repo = (UserEntity) adminsDB.getByUid(chatId);
                    String id = repo.getTgId();
                    String lang = repo.getLanguage();

                    String text = switch (lang) {
                        case "ru" -> templatesDB.getByKey("ru", message);
                        case "en" -> templatesDB.getByKey("en", message);
                        case "md" -> templatesDB.getByKey("md", message);
                        default -> "";
                    };
                    SendMessage send = new SendMessage(id, manager.getText(lang, "admmsg") + "\n\n\"" + text + "\"");
                    send.setParseMode("HTML");
                    bot.execute(send);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } else {
            adminsDB.getAll().forEach(e -> {
                try {

                    if (!e.getTgId().equals(cId)) {
                        String lang = e.getLanguage();
                        String text = switch (lang) {
                            case "ru" -> templatesDB.getByKey("ru", message);
                            case "en" -> templatesDB.getByKey("en", message);
                            case "md" -> templatesDB.getByKey("md", message);
                            default -> "";
                        };

                        SendMessage send = new SendMessage(e.getTgId(), manager.getText(lang, "admmsg") + "\n\n\"" + text + "\"");
                        send.setParseMode("HTML");
                        bot.execute(send);
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            });
        }
        adminsDB.update("state", "none", m.getFrom().getId().toString());
        bot.execute(new SendMessage(cId, "Сообщения доставлены!"));
        isUid = false;
    }
}