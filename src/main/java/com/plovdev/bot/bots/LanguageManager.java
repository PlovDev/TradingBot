package com.plovdev.bot.bots;

import com.plovdev.org.XMLDocument;
import com.plovdev.org.read.XMLReader;

/**
 * Класс обеспечивающий доступ к локализированным текстовым сообщениям.
 */
public class LanguageManager {

    // Создаем XML документы для каждого языкового пакета.
    private final XMLDocument ruDoc = new XMLDocument(getClass().getResourceAsStream("/langs/rus.xml"));
    private final XMLDocument enDoc = new XMLDocument(getClass().getResourceAsStream("/langs/eng.xml"));
    private final XMLDocument mdDoc = new XMLDocument(getClass().getResourceAsStream("/langs/mdl.xml"));

    // Создаем читатели этих языковых пакетов.
    private final XMLReader ruReader = ruDoc.getXMLReader();
    private final XMLReader enReader = enDoc.getXMLReader();
    private final XMLReader mdReader = mdDoc.getXMLReader();


    /**
     * Метод, который дает локализированный текст по ключу, в зависимости от языка.
     * @param lang - язык, на котором нужно будет вернуть текст.
     * @param key - что именно возвращать из языкового пакета (XPath синтаксис).
     * @return - возвращает локализированный текст.
     */
    public String getText(String lang, String key) {
        key = "/bot/" + key;

        return switch (lang) {
            case "ru" -> ruReader.getElements(key).getFirst().replace("    ", ""); // replace для того, чтобы нормализировать
            case "en" -> enReader.getElements(key).getFirst().replace("    ", ""); // возвращаемый текст.
            case "md" -> mdReader.getElements(key).getFirst().replace("    ", "");
            case null -> ruReader.getElements(key).getFirst().replace("    ", ""); // если язый не определен.
            default -> throw new RuntimeException("Text can't be null!");
        };
    }
}
