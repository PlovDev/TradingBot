package com.plovdev.bot.modules.beerjes.utils;

import com.plovdev.bot.modules.beerjes.TakeProfitLevel;
import com.plovdev.bot.modules.beerjes.TradeService;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.exceptions.InvalidParametresException;
import com.plovdev.bot.modules.models.OrderResult;
import com.plovdev.bot.modules.models.SettingsService;
import com.plovdev.bot.modules.models.SymbolInfo;
import com.plovdev.bot.modules.parsers.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Класс, предосталяющий общие утилитарные средства по работе с биржам.
 */
public class BeerjUtils {
    private static final BigDecimal MIN_ORDER_SIZE = new BigDecimal("2.0");
    private static final SettingsService service = new SettingsService();
    private static final Logger logger = LoggerFactory.getLogger("BeerjUtils");

    /**
     * Приватный конструктор.
     */
    private BeerjUtils() {
    }

    /**
     * Находит проценты из параметров.
     *
     * @param percents кол-во процентов
     * @param number   начальное число для поиска.
     * @return переведенные проценты в USDT.
     */
    public static BigDecimal getPercent(BigDecimal percents, BigDecimal number) {
        return (number.divide(new BigDecimal("100.0"), 10, RoundingMode.HALF_UP)).multiply(percents);
    }


    //Словарь монет-исключений для BitGet
    private static final Map<String, String> bitGetTokens = loadBgTokens();
    private static final Map<String, String> bitunixTokens = loadBuTokens();
    //Константы с названий бирж
    public static final String BITGET = "bitget";
    public static final String BITUNIX = "bitunix";

    private static Map<String, String> loadBgTokens() {
        Map<String, String> map = new HashMap<>();
        map.put("shib", "shib");
        map.put("luna", "luna");
        map.put("lunc", "lunc");
        map.put("floki", "floki");
        map.put("pepe", "pepe");
        map.put("beam", "beam");
        map.put("agi", "agi");
        map.put("cheems", "cheems");
        map.put("tst", "tst");
        map.put("lay", "lay");
        return map;
    }

    private static Map<String, String> loadBuTokens() {
        Map<String, String> map = new HashMap<>();
        map.put("shib", "1000shib");
        map.put("luna", "luna");
        map.put("lunc", "1000lunc");
        map.put("floki", "1000floki");
        map.put("pepe", "1000pepe");
        map.put("beam", "beamx");
        map.put("agi", "agix");
        map.put("cheems", "1000cheems");
        map.put("tst", "tst");
        map.put("lay", "lay");
        return map;
    }

    /**
     * Возвращает правильное название монеты под указанную биржу, пример:
     * BitGet - SHIBUSDT
     * BitUnix - 1000SHIBUSDT
     * вход - SHIBUSDT, exchange - bitunix
     * выход - 1000SHINUSDT
     *
     * @param baseName пара из сигнала.
     * @param exch     биржа для которой брать название.
     * @return правильная пара под нужную биржу.
     */
    public static String getExchangeCoin(String baseName, String exch) {
        String name = baseName.toUpperCase().replace("USDT", "");
        logger.info("Получена монета: {}", name);

        // Обрабатываем биржи
        if (exch.equalsIgnoreCase(BITGET)) {
            logger.info("Смотрми на список bitget");
            //BitGet монеты
            return parseExchange(bitGetTokens, name).toUpperCase();
        } else if (exch.equalsIgnoreCase(BITUNIX)) {
            logger.info("Смотрми на список bitunix");
            //BitUnix монеты
            return parseExchange(bitunixTokens, name).toUpperCase();
        } else {
            throw new InvalidParametresException("Unknow exchange: " + exch);
        }
    }

    /**
     * Ищет монету в списке.
     *
     * @param map  список монет у конкретной биржи.
     * @param name название монеты из сигнала
     * @return правильное название монеты в нужной биржи.
     */
    private static String parseExchange(Map<String, String> map, String name) {
        name = name.replace("1000", "").toLowerCase();
        logger.info("Ищем пару в бирже, coin: {}", name);

        if (map.containsKey(name)) {
            String coin = map.get(name);
            String total = coin + "USDT";
            logger.info("Найдена монета: {}", total);
            return total;
        } else return name.toUpperCase() + "USDT";
    }

    /**
     * Распределяет общий размер позиции по уровням тейк-профита с учётом:
     * - Настроек админа (процентное распределение)
     * - Целочисленности размеров ордеров (в USDT)
     * - Направления сделки (LONG/SHORT)
     * - Текущей рыночной цены (фильтрация невалидных тейков)
     *
     * @param signal        Сигнал с информацией о направлении и ценах тейк-профитов
     * @param totalSize     Общий размер позиции в USDT (целое положительное число)
     * @param tpRatios      Список процентов для каждого тейка (сумма = 100%)
     * @param currentPrice  Текущая рыночная цена (mark price)
     * @return Список валидных уровней тейк-профита с целочисленными размерами
     */
    public static List<TakeProfitLevel> adjustTakeProfits(Signal signal, BigDecimal totalSize, List<BigDecimal> tpRatios, BigDecimal currentPrice, SymbolInfo symbolInfo) {
        int pricePlace = symbolInfo.getPricePlace();
        int volumePlace = symbolInfo.getVolumePlace();
        BigDecimal minOrderSize = symbolInfo.getMinTradeNum();

        List<TakeProfitLevel> result = new ArrayList<>();
        if (totalSize == null || totalSize.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Total size is null, or equal zero. returning...");
            return result;
        }

        boolean isLong = "LONG".equalsIgnoreCase(signal.getDirection());

        List<BigDecimal> signalTargets = signal.getTargets().stream().filter(t -> {
            if (isLong) {
                return t.compareTo(currentPrice) > 0;
            } else {
                return t.compareTo(currentPrice) < 0;
            }
        }).toList();
        logger.info("Targets: {}", signalTargets);
        if (signalTargets.isEmpty() || tpRatios == null || tpRatios.isEmpty()) {
            logger.warn("Empty data: signal targets, tp ratios, returning...");
            return result;
        }



        int levelsToUse = Math.min(tpRatios.size(), signalTargets.size());
        BigDecimal usedSize = BigDecimal.ZERO;
        logger.info("Data: isLong: {}, levelToUse: {}, volumePlace: {}, pricePlace: {}", isLong, levelsToUse, volumePlace, pricePlace);
        logger.info("Current price is: {}", currentPrice);

        // 1. Фильтруем и создаём уровни только для валидных цен
        for (int i = 0; i < levelsToUse; i++) {
            BigDecimal tpPrice = signalTargets.get(i);
            if (tpPrice == null) continue;
            System.out.println(tpPrice);
            tpPrice = tpPrice.setScale(pricePlace, RoundingMode.HALF_EVEN);

            // Проверяем валидность цены тейка относительно направления и текущей цены
            logger.info("Take price: {}", tpPrice);
            boolean isValidPrice = (isLong && tpPrice.compareTo(currentPrice) > 0) || (!isLong && tpPrice.compareTo(currentPrice) < 0);

            if (!isValidPrice) {
                logger.warn("Price is invalid.");
                continue; // Пропускаем невалидные тейки
            }

            // Рассчитываем размер в USDT по проценту
            BigDecimal ratio = tpRatios.get(i);
            BigDecimal orderSize = totalSize.multiply(ratio).divide(BigDecimal.valueOf(100), 5, RoundingMode.HALF_UP);

            // Округляем до целого числа (в меньшую сторону, чтобы не превысить totalSize)
            BigDecimal integerSize = orderSize.setScale(volumePlace, RoundingMode.HALF_EVEN);

            if (integerSize.compareTo(minOrderSize) >= 0) {
                result.add(new TakeProfitLevel(integerSize, tpPrice));
                logger.info("Take size: {}", integerSize);
                usedSize = usedSize.add(integerSize);
            } else {
                result.add(new TakeProfitLevel(minOrderSize, tpPrice));
                logger.info("Take size is 1");
                usedSize = usedSize.add(minOrderSize);
            }
            // Если размер меньше 1 USDT — пропускаем (не добавляем в результат)
        }

        List<TakeProfitLevel> levels = reAdjustTakeProfits(totalSize, result, symbolInfo, currentPrice, signal.getDirection());
        if (levels.isEmpty()) {
            BigDecimal newPrice = currentPrice.multiply(new BigDecimal("0.01")).setScale(symbolInfo.getPricePlace(), RoundingMode.HALF_EVEN);
            levels.add(new TakeProfitLevel(totalSize, currentPrice.add(newPrice)));
            return levels;
        }

        return levels;
    }

    public static BigDecimal calculateNewStopPrice(String side, BigDecimal entry, BigDecimal offsetPercent, int pricePlace) {
        BigDecimal offsetMultiplier = offsetPercent.divide(new BigDecimal("100"), 15, RoundingMode.HALF_EVEN);
        logger.info("Calculating new stop price. Params: offsetMultyplier {}, side: {}, entry price: {}, offset percent: {}, price place: {}", offsetMultiplier, side, entry, offsetPercent, pricePlace);

        if ("SELL".equalsIgnoreCase(side) || "SHORT".equalsIgnoreCase(side)) {
            return entry.subtract(entry.multiply(offsetMultiplier)).setScale(pricePlace, RoundingMode.HALF_EVEN);
        } else if ("BUY".equalsIgnoreCase(side) || "LONG".equalsIgnoreCase(side)) {
            logger.info("Long position stop calculating...");
            return entry.add(entry.multiply(offsetMultiplier)).setScale(pricePlace, RoundingMode.HALF_EVEN);
        }
        return entry;
    }


    public static List<TakeProfitLevel> reAdjustTakeProfits(BigDecimal totalSize, List<TakeProfitLevel> takeProfitLevels, SymbolInfo info, BigDecimal currentPrice, String side) {
        List<TakeProfitLevel> levels = new ArrayList<>(takeProfitLevels);
        logger.info("🔄 Starting re-adjustment. Total size: {}, Takes: {}",
                totalSize, levels);

        if (levels.isEmpty()) {
            logger.warn("No take profit levels to adjust");
            return levels;
        }

        // 1. Вычисляем текущую сумму всех тейков
        BigDecimal currentTotal = levels.stream()
                .map(TakeProfitLevel::getSize)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        logger.info("Current total: {}, Target total: {}", currentTotal, totalSize);

        // 2. Если сумма уже равна totalSize - ничего не делаем
        if (currentTotal.compareTo(totalSize) == 0) {
            BigDecimal muliplierUsed = BigDecimal.ZERO;
            logger.info("CT == TS, levels: {}", levels);
            for (TakeProfitLevel level : levels) {
                BigDecimal sizeMultiplier = info.getSizeMultiplier();
                BigDecimal size = level.getSize();
                logger.info("Size: {}, Size multiplier: {}", size, sizeMultiplier);
                if (!BeerjUtils.isMultiple(size, sizeMultiplier)) {
                    size = size.divide(sizeMultiplier, info.getVolumePlace(), RoundingMode.HALF_EVEN).multiply(sizeMultiplier);
                    if (size.compareTo(info.getMinTradeNum()) < 0) {
                        size = info.getMinTradeNum();
                    }
                    muliplierUsed = muliplierUsed.add(size);
                    level.setSize(size);
                }
            }
            if (muliplierUsed.compareTo(BigDecimal.ZERO) > 0) {
                TakeProfitLevel l1 = levels.getLast();
                l1.setSize(l1.getSize().add(totalSize.subtract(muliplierUsed)));
            }

            List<TakeProfitLevel> finalResilt = getTakeProfitLevels(info, levels);

            logger.info("Sum already equals total size, no adjustment needed");
            System.out.println("LEVELS: " + finalResilt);
            //return compareLevels(finalResilt, side);
            return compareLevels(levels, side);
        }

        // 3. Вычисляем разницу
        BigDecimal difference = totalSize.subtract(currentTotal);
        logger.info("Difference to distribute: {}", difference);

        // 4. Распределяем разницу
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            // Нужно добавить разницу
            levels = distributeAddition(levels, difference, totalSize, info.getVolumePlace());
        } else {
            // Нужно убрать разницу
            levels = distributeSubtraction(levels, difference.abs(), totalSize, info.getVolumePlace());
        }

        if (levels.isEmpty()) {
            BigDecimal newPrice = currentPrice.multiply(new BigDecimal("0.01")).setScale(info.getPricePlace(), RoundingMode.HALF_EVEN);
            levels.add(new TakeProfitLevel(totalSize, currentPrice.add(newPrice)));
            return levels;
        }

        return reAdjustTakeProfits(totalSize, levels, info, currentPrice, side);
    }

    public static List<TakeProfitLevel> getTakeProfitLevels(SymbolInfo info, List<TakeProfitLevel> levels) {
        List<TakeProfitLevel> finalResilt = new ArrayList<>();
        for (int i = levels.size()-1; i >= 0; i--) {
            TakeProfitLevel level = levels.get(i);
            if (level.getSize().compareTo(info.getMinTradeNum()) < 0) {
                if (i-1 >= 0) {
                    TakeProfitLevel nextLevel = levels.get(i - 1);
                    nextLevel.setSize(nextLevel.getSize().add(level.getSize()));
                }
            } else {
                finalResilt.add(level);
            }
        }
        finalResilt = finalResilt.reversed();
        return finalResilt;
    }

    /**
     * Распределяет добавление разницы между тейками
     */
    private static List<TakeProfitLevel> distributeAddition(List<TakeProfitLevel> levels, BigDecimal amountToAdd, BigDecimal totalSize, int vol) {
        logger.info("➕ Distributing addition: {} across {} levels", amountToAdd, levels.size());

        List<TakeProfitLevel> result = new ArrayList<>();
        int levelsCount = levels.size();

        // Сначала распределяем поровну (целые части)
        BigDecimal baseAddition = amountToAdd.divide(BigDecimal.valueOf(levelsCount), vol, RoundingMode.HALF_EVEN);
        BigDecimal remainder = amountToAdd.remainder(BigDecimal.valueOf(levelsCount));

        logger.info("Base addition: {}, Remainder: {}", baseAddition, remainder);

        // Применяем базовое добавление ко всем уровням
        for (TakeProfitLevel level : levels) {
            BigDecimal newSize = level.getSize().add(baseAddition);
            result.add(new TakeProfitLevel(newSize, level.getPrice()));
        }

        // Распределяем остаток по одному на каждый уровень (начиная с первого)
        if (remainder.compareTo(BigDecimal.ZERO) > 0) {
            logger.info("Distributing remainder: {}", remainder);
            for (int i = 0; i < remainder.intValue() && i < result.size(); i++) {
                TakeProfitLevel level = result.get(i);
                BigDecimal newSize = level.getSize().add(BigDecimal.ONE);
                level.setSize(newSize);
            }
        }

        // Финальная проверка и корректировка если нужно
        return finalAdjustment(result, totalSize);
    }

    /**
     * Распределяет вычитание разницы между тейками
     */
    private static List<TakeProfitLevel> distributeSubtraction(List<TakeProfitLevel> levels, BigDecimal amountToSubtract, BigDecimal totalSize, int vol) {
        logger.info("➖ Distributing subtraction: {} across {} levels", amountToSubtract, levels.size());

        List<TakeProfitLevel> result = new ArrayList<>(levels);
        BigDecimal remainingToSubtract = amountToSubtract;

        int iters = 0;

        // Продолжаем пока есть что вычитать
        while (remainingToSubtract.compareTo(BigDecimal.ZERO) > 0 && !result.isEmpty()) {
            if (iters >= 100) {
                return new ArrayList<>();
            }
            // Считаем сколько можем вычитать с каждого уровня за эту итерацию
            int subtractPerLevel = Math.min(
                    remainingToSubtract.divide(BigDecimal.valueOf(result.size()), vol, RoundingMode.DOWN).intValue(),
                    1 // Максимум 1 за итерацию чтобы не уйти в отрицательные числа
            );

            if (subtractPerLevel == 0) {
                subtractPerLevel = 1; // Минимум 1 если есть что вычитать
            }

            logger.info("Subtracting {} per level, remaining: {}", subtractPerLevel, remainingToSubtract);

            // Вычитаем с каждого уровня
            result = result.reversed();
            for (int i = 0; i < result.size() && remainingToSubtract.compareTo(BigDecimal.ZERO) > 0; i++) {
                TakeProfitLevel level = result.get(i);
                BigDecimal currentSize = level.getSize();

                // Проверяем что после вычитания размер будет положительным
                if (currentSize.compareTo(BigDecimal.ONE) >= 0) {
                    BigDecimal newSize = currentSize.subtract(BigDecimal.valueOf(subtractPerLevel));
                    level.setSize(newSize);
                    remainingToSubtract = remainingToSubtract.subtract(BigDecimal.valueOf(subtractPerLevel));
                }
            }
            result = result.reversed();

            // Убираем уровни с нулевым размером
            result.removeIf(level -> level.getSize().compareTo(BigDecimal.ZERO) <= 0);
            iters++;
        }

        return finalAdjustment(result, totalSize);
    }

    /**
     * Финальная корректировка чтобы сумма была АБСОЛЮТНО равна totalSize
     */
    private static List<TakeProfitLevel> finalAdjustment(List<TakeProfitLevel> levels, BigDecimal totalSize) {
        BigDecimal currentTotal = levels.stream()
                .map(TakeProfitLevel::getSize)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalDifference = totalSize.subtract(currentTotal);
        System.out.println("Fial levels: " + levels);

        if (finalDifference.compareTo(BigDecimal.ZERO) == 0) {
            logger.info("✅ Final adjustment: Sum equals total size");
            return levels;
        }

        logger.info("🔧 Final adjustment needed: {}", finalDifference);

        // Просто добавляем/убираем разницу с первого уровня
        if (finalDifference.compareTo(BigDecimal.ZERO) > 0) {
            TakeProfitLevel firstLevel = levels.getFirst();
            BigDecimal newSize = firstLevel.getSize().add(finalDifference);
            firstLevel.setSize(newSize);
            logger.info("Added {} to first level", finalDifference);
        } else {
            TakeProfitLevel firstLevel = levels.getFirst();
            BigDecimal subtraction = finalDifference.abs();
            if (firstLevel.getSize().compareTo(subtraction) >= 0) {
                BigDecimal newSize = firstLevel.getSize().subtract(subtraction);
                firstLevel.setSize(newSize);
                logger.info("Subtracted {} from first level", subtraction);
            }
        }

        // Финальная проверка
        BigDecimal finalTotal = levels.stream()
                .map(TakeProfitLevel::getSize)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        logger.info("🎯 Final result: {} levels, total: {} (target: {})",
                levels.size(), finalTotal, totalSize);

        return levels;
    }


    public static List<TakeProfitLevel> getMarginLevels(List<TakeProfitLevel> tpLevels, BigDecimal margin) {
        BigDecimal totalMargin = BigDecimal.ZERO;
        for (TakeProfitLevel tp : tpLevels) totalMargin = totalMargin.add(tp.getSize());
        if (totalMargin.compareTo(margin) > 0) {
            List<TakeProfitLevel> subList = tpLevels.subList(0, tpLevels.size() - 1);
            return getMarginLevels(subList, margin);
        } else {
            return tpLevels;
        }
    }

    public static OrderResult valdateOpen(UserEntity user, Signal signal) {
        String srcFrom = signal.getSrc().toLowerCase();
        String strategy = user.getGroup().toLowerCase();
        logger.info("Validating user");
        logger.info("Src from: {}, user strategy: {}", srcFrom, strategy);

        if (srcFrom.equals("tg")) {
            if (strategy.equals("tv"))
                return new OrderResult(false, "none", signal.getSymbol(), "User group no right", List.of(), List.of());
        }
        if (srcFrom.equals("tv")) {
            if (strategy.equals("tg"))
                return new OrderResult(false, "none", signal.getSymbol(), "User group no right", List.of(), List.of());
        }

        logger.info("Check user(new positions)");
        if (!user.canOpenNewPositoin(signal)) return OrderResult.error("User: " + user.getTgId() + ", " + user.getTgName() + " already has active position for pair.", "none", signal.getSymbol());

        return OrderResult.ok("Ok", "0", signal.getSymbol());
    }

    public static List<TakeProfitLevel> compareLevels(List<TakeProfitLevel> levels, String side) {
        List<BigDecimal> sizes = new ArrayList<>();
        for (TakeProfitLevel l : levels) {
            sizes.add(l.getSize());
        }

        Collections.sort(sizes);

        // Извлекаем все цены и сортируем по возрастанию
        List<BigDecimal> prices = new ArrayList<>();
        for (TakeProfitLevel l : levels) {
            prices.add(l.getPrice());
        }

        Collections.sort(prices); // сортировка по возрастанию

        logger.info("Take adjust side: {}", side);
        if (side.equalsIgnoreCase("LONG")) {
            sizes = sizes.reversed();
        }


        // Формируем новый список: к самой маленькой цене — самый большой size, и так далее
        List<TakeProfitLevel> result = new ArrayList<>();
        for (int i = 0; i < levels.size(); i++) {
            result.add(new TakeProfitLevel(sizes.get(i), prices.get(i)));
        }

        return result;
    }

    /**
     * Проверяет, что 1 число кротно другому.
     * @param value входное число
     * @param divisor кратное число
     * @return кратно?
     */
    public static boolean isMultiple(BigDecimal value, BigDecimal divisor) {
        logger.info("Is multiple param: value: {}, divisor: {}", value, divisor);
        boolean isMultiple = value.remainder(divisor).compareTo(BigDecimal.ZERO) == 0;
        logger.info("Is multiple? - {}", isMultiple);
        return isMultiple;
    }

    public static BigDecimal getPosSize(UserEntity user, Signal signal, TradeService service, BigDecimal entryPrice) {
        try {
            BigDecimal stopLoss = new BigDecimal(signal.getStopLoss());

            return service.calculatePositionSize(user, entryPrice, stopLoss, signal.getDirection());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }









    public static List<TakeProfitLevel> reAdjustTakeProfitsBU(BigDecimal totalSize, List<TakeProfitLevel> takeProfitLevels, SymbolInfo info, BigDecimal currentPrice, String side) {
        List<TakeProfitLevel> levels = new ArrayList<>(takeProfitLevels);
        logger.info("🔄 Starting re-adjustment. Total size: {}, Takes: {}",
                totalSize, levels);

        if (levels.isEmpty()) {
            logger.warn("No take profit levels to adjust");
            return levels;
        }

        // 1. Вычисляем текущую сумму всех тейков
        BigDecimal currentTotal = levels.stream()
                .map(TakeProfitLevel::getSize)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        logger.info("Current total: {}, Target total: {}", currentTotal, totalSize);

        // 2. Если сумма уже равна totalSize - ничего не делаем
        if (currentTotal.compareTo(totalSize) == 0) {
            BigDecimal muliplierUsed = BigDecimal.ZERO;
            logger.info("CT == TS, levels: {}", levels);
            for (TakeProfitLevel level : levels) {
                BigDecimal sizeMultiplier = info.getSizeMultiplier();
                BigDecimal size = level.getSize();
                logger.info("Size: {}, Size multiplier: {}", size, sizeMultiplier);
                if (!BeerjUtils.isMultiple(size, sizeMultiplier)) {
                    size = size.divide(sizeMultiplier, info.getVolumePlace(), RoundingMode.HALF_EVEN).multiply(sizeMultiplier);
                    if (size.compareTo(info.getMinTradeNum()) < 0) {
                        size = info.getMinTradeNum();
                    }
                    muliplierUsed = muliplierUsed.add(size);
                    level.setSize(size);
                }
            }
            if (muliplierUsed.compareTo(BigDecimal.ZERO) > 0) {
                TakeProfitLevel l1 = levels.getLast();
                l1.setSize(l1.getSize().add(totalSize.subtract(muliplierUsed)));
            }

            List<TakeProfitLevel> finalResilt = getTakeProfitLevels(info, levels);

            logger.info("Sum already equals total size, no adjustment needed");
            System.out.println("LEVELS: " + finalResilt);


            // ... (после всей вашей логики распределения и округления в reAdjustTakeProfits)

// --- !!! НАЧАЛО КРИТИЧЕСКОГО ИСПРАВЛЕНИЯ ROUNDING !!! ---

// 1. Вычисляем сумму всех новых объемов после округления
            BigDecimal finalRecalculatedTotal = levels.stream()
                    .map(TakeProfitLevel::getSize)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

// 2. Сравниваем сумму с целевым размером позиции (totalSize)
            if (finalRecalculatedTotal.compareTo(totalSize) != 0) {

                BigDecimal difference = finalRecalculatedTotal.subtract(totalSize);

                // Корректируем последний тейк, чтобы поглотить разницу (неважно, излишек или недостаток)
                TakeProfitLevel lastLevel = levels.getLast();

                BigDecimal newLastLevelSize = lastLevel.getSize().subtract(difference);

                // Логирование для отслеживания
                logger.warn("⚠️ Rounding Correction: Final TP total ({}) != Target position ({}). Difference: {}. Correcting last level size from {} to {}.",
                        finalRecalculatedTotal, totalSize, difference, lastLevel.getSize(), newLastLevelSize);

                // Гарантируем, что размер не станет отрицательным
                if (newLastLevelSize.compareTo(BigDecimal.ZERO) < 0) {
                    newLastLevelSize = BigDecimal.ZERO;
                }

                lastLevel.setSize(newLastLevelSize);
            }

// --- !!! КОНЕЦ КРИТИЧЕСКОГО ИСПРАВЛЕНИЯ ROUNDING !!! ---

            return compareLevels(levels, side);

        }

        // 3. Вычисляем разницу
        BigDecimal difference = totalSize.subtract(currentTotal);
        logger.info("Difference to distribute: {}", difference);

        // 4. Распределяем разницу
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            // Нужно добавить разницу
            levels = distributeAddition(levels, difference, totalSize, info.getVolumePlace());
        } else {
            // Нужно убрать разницу
            levels = distributeSubtraction(levels, difference.abs(), totalSize, info.getVolumePlace());
        }

        if (levels.isEmpty()) {
            BigDecimal newPrice = currentPrice.multiply(new BigDecimal("0.01")).setScale(info.getPricePlace(), RoundingMode.HALF_EVEN);
            levels.add(new TakeProfitLevel(totalSize, currentPrice.add(newPrice)));
            return levels;
        }

        return reAdjustTakeProfits(totalSize, levels, info, currentPrice, side);
    }
}