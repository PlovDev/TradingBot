package com.plovdev.bot.modules.parsers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelegramSignalParser {
    public static Signal parse(String signalText) {
        String symbol = "";
        String direction = "";
        List<String> types = new ArrayList<>();
        List<BigDecimal> targets = new ArrayList<>();
        String stopLoss = "0";


        // Парсинг символа
        Pattern symbolPattern = Pattern.compile("ℹ️\\s*(\\w+/?\\w+)");
        Matcher symbolMatcher = symbolPattern.matcher(signalText);
        if (symbolMatcher.find()) {
            symbol = symbolMatcher.group(1);
        }

        // Парсинг направления (SHORT/LONG)
        Pattern directionPattern = Pattern.compile("(🟢\\s*LONG|🔴\\s*SHORT)", Pattern.CASE_INSENSITIVE);
        Matcher directionMatcher = directionPattern.matcher(signalText);
        if (directionMatcher.find()) {
            String dir = directionMatcher.group(1);
            direction = dir.contains("LONG") ? "LONG" : "SHORT";
        }

        Pattern typePattern = Pattern.compile(".(\uD83D\uDD31\\s*market|\uD83D\uDD31\\s*\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher typeMatcher = typePattern.matcher(signalText);
        while (typeMatcher.find()) {
            String tp = typeMatcher.group(1).substring(2).trim().toLowerCase();
            types.add(tp);
        }

        // Парсинг целей (targets)
        Pattern targetPattern = Pattern.compile("🎯\\s*(\\d+\\.\\d+)");
        Matcher targetMatcher = targetPattern.matcher(signalText);
        while (targetMatcher.find()) {
            targets.add(new BigDecimal(targetMatcher.group(1)));
        }

        // Парсинг стоп-лосса
        Pattern stopLossPattern = Pattern.compile("[⛔|⛔️]\\s*(\\d+\\.*\\d+)");
        Matcher stopLossMatcher = stopLossPattern.matcher(signalText);
        if (stopLossMatcher.find()) {
            stopLoss = stopLossMatcher.group(1);
        }
        Collections.sort(targets);
        return new Signal("tg", symbol.replace("/", ""), null, direction, types, stopLoss, targets, "tg", null, 0);
    }

    public static boolean validate(String signalText) {
        boolean symbol;
        boolean direction;
        boolean stopLoss;


        // Парсинг символа
        Pattern symbolPattern = Pattern.compile("ℹ️\\s*(\\w+/?\\w+)");
        Matcher symbolMatcher = symbolPattern.matcher(signalText);
        symbol = symbolMatcher.find();

        // Парсинг направления (SHORT/LONG)
        Pattern directionPattern = Pattern.compile("(🟢\\s*LONG|🔴\\s*SHORT)", Pattern.CASE_INSENSITIVE);
        Matcher directionMatcher = directionPattern.matcher(signalText);
        direction = directionMatcher.find();

        // Парсинг стоп-лосса
        Pattern stopLossPattern = Pattern.compile("[⛔|⛔️]\\s*(\\d+.*\\d+)");
        Matcher stopLossMatcher = stopLossPattern.matcher(signalText);
        stopLoss = stopLossMatcher.find();

        return symbol && direction && stopLoss;
    }
}