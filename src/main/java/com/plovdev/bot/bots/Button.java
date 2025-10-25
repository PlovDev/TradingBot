package com.plovdev.bot.bots;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

/**
 * Класс, расширяющий InlineKeyboardButton, и обеспечивающий большее удобство
 * для работы с inline кнопками в ТГ.
 */
public class Button extends InlineKeyboardButton {
    /**
     * Стандартный конструктор Button
     */
    public Button() {
        super();
    }

    /**
     * Конструктор кнопки, содержащий текст.
     * @param text - тект кнопки.
     */
    public Button(String text) {
        super(text);
    }

    /**
     * Конструктор кнопки, содержащий текст и ее калбек.
     * @param text - текст кнопки.
     * @param callback - калбек кнопки.
     */
    public Button(String text, String callback) {
        super(text);
        setCallbackData(callback);
    }


    /**
     * Конструктор кнопки, содержащий текст, слушатель и ее калбек.
     * @param text - текст кнопки.
     * @param callback - калбек кнопки.
     * @param listener - слушатель кнопки.
     */
    public Button(String text, String callback, ComandListener listener) {
        super(text);
        setCallbackData(callback);
        CommandHandler.registerComand(getCallbackData(), listener);
    }


    /**
     * Устанавлваем слушателя для кнопки.
     * @param listener - слушатель, который выпониться при нажатии на кнопку в ТГ.
     */
    public void setActionListener(ComandListener listener) {
        CommandHandler.registerComand(getCallbackData(), listener);
    }
}
