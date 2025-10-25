package com.plovdev.bot.modules.parsers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TradingViewSignalParser {
    public static Signal parse(String signalText) {
        String symbol = "";
        String direction = "";
        List<BigDecimal> targets = new ArrayList<>();
        String stopLoss = "0";
        int priority = 0;


        // Парсинг символа
        Pattern symbolPattern = Pattern.compile("\uD83D\uDCE9\\s*#(\\w+[.P]?)");
        Matcher symbolMatcher = symbolPattern.matcher(signalText);
        if (symbolMatcher.find()) {
            symbol = symbolMatcher.group(1);
        }

        // Парсинг направления (SHORT/LONG)
        Pattern directionPattern = Pattern.compile("(?i)(BUY|SELL)");
        Matcher directionMatcher = directionPattern.matcher(signalText);
        if (directionMatcher.find()) {
            String dir = directionMatcher.group(1);
            direction = dir.contains("SELL") ? "SHORT" : "LONG";
        }

        Pattern priorityPattern = Pattern.compile("Accuracy\\s*of\\s*this\\s*strategy\\s*:\\s*([0-9]+)");
        Matcher priorityMatcher = priorityPattern.matcher(signalText);
        if (priorityMatcher.find()) {
            priority = Integer.parseInt(priorityMatcher.group(1));
        }


        // Парсинг целей (targets)
        Pattern targetPattern = Pattern.compile("Target\\s*\\d+\\s*:\\s*(\\d+.\\d+)");
        Matcher targetMatcher = targetPattern.matcher(signalText);
        while (targetMatcher.find()) {
            targets.add(new BigDecimal(targetMatcher.group(1)));
        }

        // Парсинг стоп-лосса
        Pattern stopLossPattern = Pattern.compile("Stop-Loss:\\s*([0-9.]+)");
        Matcher stopLossMatcher = stopLossPattern.matcher(signalText);
        if (stopLossMatcher.find()) {
            stopLoss = stopLossMatcher.group(1);
        }
        Collections.sort(targets);

        return new Signal("tv", symbol.replaceAll("\\.P|\\.", ""), null, direction, List.of("market"), stopLoss, targets, "tv", null, priority);
    }

    public static boolean validate(String signalText) {
        boolean symbol;
        boolean direction;
        boolean stopLoss;


        // Парсинг символа
        Pattern symbolPattern = Pattern.compile("\uD83D\uDCE9\\s*#(\\w+[.P]?)");
        Matcher symbolMatcher = symbolPattern.matcher(signalText);
        symbol = symbolMatcher.find();

        // Парсинг направления (SHORT/LONG)
        Pattern directionPattern = Pattern.compile("(?i)(BUY|SELL)");
        Matcher directionMatcher = directionPattern.matcher(signalText);
        direction = directionMatcher.find();

        // Парсинг стоп-лосса
        Pattern stopLossPattern = Pattern.compile("Stop-Loss:\\s*([0-9.]+)");
        Matcher stopLossMatcher = stopLossPattern.matcher(signalText);
        stopLoss = stopLossMatcher.find();

        return symbol && direction && stopLoss;
    }
}