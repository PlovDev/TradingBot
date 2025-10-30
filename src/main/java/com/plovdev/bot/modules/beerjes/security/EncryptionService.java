package com.plovdev.bot.modules.beerjes.security;

/**
 * Интерфес предоставляющий криптографические иструменты.
 */
public interface EncryptionService {

    /**
     * Шифрует строку.
     * @param to входная строка
     * @return зашифрованная строка
     */
    String encrypt(String to);

    /**
     * Расшифровывает строку.
     * @param from зашированная строка
     * @return расшифрованная строка.
     */
    String decrypt(String from);

    /**
     * сохраняет ключ
     */
    void saveKey();

    /**
     * Зашружает ключ
     * @param path путь к ключу/файлу
     * @return ключ
     */
    String loadKey(String path);

    /**
     * Генерирует сигнатуру для бирж.
     * @param message строка сигнатуры.
     * @param key ключ
     * @return сигнатура.
     */
    String generateSignature(String message, String key);
}