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
        map.put("beam", "beam");
        map.put("agi", "agi");
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
     * @param signal       Сигнал с информацией о направлении и ценах тейк-профитов
     * @param totalSize    Общий размер позиции в USDT (целое положительное число)
     * @param tpRatios     Список процентов для каждого тейка (сумма = 100%)
     * @param currentPrice Текущая рыночная цена (mark price)
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

            result.add(new TakeProfitLevel(integerSize, tpPrice));
            logger.info("Take size: {}", integerSize);
            usedSize = usedSize.add(integerSize);
            // Если размер меньше 1 USDT — пропускаем (не добавляем в результат)
        }

        List<TakeProfitLevel> levels = reAdjustTakeProfits(totalSize, result, symbolInfo, currentPrice, signal.getDirection());
        if (levels.isEmpty()) {
            levels.add(new TakeProfitLevel(totalSize, signal.getTargets().getFirst()));
        }
        for (TakeProfitLevel level : levels) {
            level.setSize(level.getSize().setScale(symbolInfo.getVolumePlace(), RoundingMode.HALF_EVEN));
            level.setPrice(level.getPrice().setScale(symbolInfo.getPricePlace(), RoundingMode.HALF_EVEN));
        }

        return compareLevels(levels, signal.getDirection());
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


    /**
     * Перераспределение тейков с учётом minTradeNum, sizeMultiplier и округлений.
     */
    public static List<TakeProfitLevel> reAdjustTakeProfits(BigDecimal totalSize, List<TakeProfitLevel> takeProfitLevels, SymbolInfo info, BigDecimal currentPrice, String side) {
        List<TakeProfitLevel> levels = new ArrayList<>(takeProfitLevels);
        logger.info("🔄 Starting re-adjustment. Total size: {}, Takes: {}", totalSize, levels);

        if (levels.isEmpty()) {
            logger.warn("No take profit levels to adjust");
            return levels;
        }

        // 1. Проверяем и корректируем размеры под sizeMultiplier
        levels = ensureSizeMultiplier(levels, info);

        // 2. Считаем текущую сумму
        BigDecimal currentTotal = levels.stream().map(TakeProfitLevel::getSize).reduce(BigDecimal.ZERO, BigDecimal::add);
        logger.info("Current total: {}, Target total: {}", currentTotal, totalSize);

        // 3. Если сумма совпадает, просто возвращаем
//        if (currentTotal.compareTo(totalSize) == 0) {
//            logger.info("✅ Sum already equals total size, no adjustment needed");
//            return levels;
//        }

        // 4. Разница
        BigDecimal difference = totalSize.subtract(currentTotal);
        logger.info("Difference to distribute: {}", difference);

        // 5. Распределяем разницу
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            levels = distributeAddition(levels, difference, info);
        } else {
            levels = distributeSubtraction(levels, difference.abs(), info);
        }

        // 6. Убираем уровни < minTradeNum, добавляя к предыдущему
        logger.info("Merge levels: {}", levels);
        levels = mergeLevelsIfBelowMin(levels, info);

        // 7. Финальная проверка
        BigDecimal finalTotal = levels.stream().map(TakeProfitLevel::getSize).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (finalTotal.compareTo(totalSize) != 0) {
            // Если всё равно не сошлось, доводим до totalSize
            finalAdjustment(levels, totalSize);
        }

        return levels;
    }

    private static List<TakeProfitLevel> ensureSizeMultiplier(List<TakeProfitLevel> levels, SymbolInfo info) {
        BigDecimal sizeMultiplier = info.getSizeMultiplier();
        BigDecimal minTradeNum = info.getMinTradeNum();
        List<TakeProfitLevel> result = new ArrayList<>();
        for (TakeProfitLevel level : levels) {
            BigDecimal size = level.getSize();
            if (!isMultiple(size, sizeMultiplier)) {
                // Округляем вниз до кратности
                BigDecimal adjustedSize = size.divide(sizeMultiplier, 0, RoundingMode.FLOOR).multiply(sizeMultiplier);
                if (adjustedSize.compareTo(minTradeNum) < 0) {
                    adjustedSize = minTradeNum; // не ниже min
                }
                level.setSize(adjustedSize);
            }
            result.add(level);
        }
        return result;
    }


    private static List<TakeProfitLevel> distributeAddition(List<TakeProfitLevel> levels, BigDecimal amountToAdd, SymbolInfo info) {
        logger.info("➕ Distributing addition: {} across {} levels", amountToAdd, levels.size());
        if (amountToAdd.compareTo(BigDecimal.ZERO) <= 0) return levels;

        List<TakeProfitLevel> result = new ArrayList<>(levels);
        BigDecimal remainingToAdd = amountToAdd;

        while (remainingToAdd.compareTo(BigDecimal.ZERO) > 0) {
            int eligibleLevels = (int) result.stream().filter(l -> l.getSize().compareTo(info.getMinTradeNum()) >= 0).count();
            if (eligibleLevels == 0) break;

            BigDecimal addPerLevel = remainingToAdd.divide(BigDecimal.valueOf(eligibleLevels), info.getVolumePlace(), RoundingMode.DOWN);
            if (addPerLevel.compareTo(BigDecimal.ZERO) == 0) addPerLevel = info.getSizeMultiplier().max(BigDecimal.ONE);

            BigDecimal actualAdded = BigDecimal.ZERO;
            for (TakeProfitLevel level : result) {
                if (remainingToAdd.compareTo(BigDecimal.ZERO) <= 0) break;
                if (level.getSize().compareTo(info.getMinTradeNum()) < 0) continue;

                BigDecimal toAdd = addPerLevel.min(remainingToAdd);
                level.setSize(level.getSize().add(toAdd));
                actualAdded = actualAdded.add(toAdd);
                remainingToAdd = remainingToAdd.subtract(toAdd);
            }
            if (actualAdded.compareTo(BigDecimal.ZERO) == 0) break;
        }

        return result;
    }

    private static List<TakeProfitLevel> distributeSubtraction(List<TakeProfitLevel> levels, BigDecimal amountToSubtract, SymbolInfo info) {
        logger.info("➖ Distributing subtraction: {} across {} levels", amountToSubtract, levels.size());
        if (amountToSubtract.compareTo(BigDecimal.ZERO) <= 0) return levels;

        List<TakeProfitLevel> result = new ArrayList<>(levels);
        BigDecimal remainingToSubtract = amountToSubtract;

        while (remainingToSubtract.compareTo(BigDecimal.ZERO) > 0) {
            int eligibleLevels = (int) result.stream().filter(l -> l.getSize().compareTo(info.getMinTradeNum()) > 0).count();
            if (eligibleLevels == 0) break;

            BigDecimal subPerLevel = remainingToSubtract.divide(BigDecimal.valueOf(eligibleLevels), info.getVolumePlace(), RoundingMode.UP);
            if (subPerLevel.compareTo(BigDecimal.ZERO) == 0) subPerLevel = info.getSizeMultiplier().max(BigDecimal.ONE);

            BigDecimal actualSubtracted = BigDecimal.ZERO;
            for (TakeProfitLevel level : result) {
                if (remainingToSubtract.compareTo(BigDecimal.ZERO) <= 0) break;
                if (level.getSize().compareTo(info.getMinTradeNum()) <= 0) continue;

                BigDecimal toSub = subPerLevel.min(level.getSize().subtract(info.getMinTradeNum())).min(remainingToSubtract);
                if (toSub.compareTo(BigDecimal.ZERO) > 0) {
                    level.setSize(level.getSize().subtract(toSub));
                    actualSubtracted = actualSubtracted.add(toSub);
                    remainingToSubtract = remainingToSubtract.subtract(toSub);
                }
            }
            if (actualSubtracted.compareTo(BigDecimal.ZERO) == 0) break;
        }

        // Убираем уровни < minTradeNum
        //result.removeIf(l -> l.getSize().compareTo(info.getMinTradeNum()) < 0);

        return result;
    }

    private static List<TakeProfitLevel> mergeLevelsIfBelowMin(List<TakeProfitLevel> levels, SymbolInfo info) {
        List<TakeProfitLevel> result = new ArrayList<>(levels); // работаем с копией
        BigDecimal minTradeNum = info.getMinTradeNum();
        logger.info("Start merge");

        for (int i = result.size() - 1; i >= 0; i--) { // идём с конца
            TakeProfitLevel currentLevel = result.get(i);
            logger.info("Level: {}, min trade num: {}", currentLevel, minTradeNum);
            if (currentLevel.getSize().compareTo(minTradeNum) < 0) { // если размер < min
                if (i > 0) { // если есть предыдущий элемент
                    TakeProfitLevel prevLevel = result.get(i - 1);
                    prevLevel.setSize(prevLevel.getSize().add(currentLevel.getSize())); // прибавляем к предыдущему
                    logger.info("Merged small level (size {}) to previous level (new size: {})", currentLevel.getSize(), prevLevel.getSize());
                }
                // Удаляем текущий уровень, т.к. он < minTradeNum
                result.remove(i);
            }
        }

        return result;
    }

    private static List<TakeProfitLevel> finalAdjustment(List<TakeProfitLevel> levels, BigDecimal totalSize) {
        if (levels.isEmpty()) return levels;

        BigDecimal currentTotal = levels.stream().map(TakeProfitLevel::getSize).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal diff = totalSize.subtract(currentTotal);

        if (diff.compareTo(BigDecimal.ZERO) == 0) return levels;

        // Добавляем разницу к ПЕРВОМУ уровню, а не к последнему
        TakeProfitLevel firstLevel = levels.getFirst();
        BigDecimal newSize = firstLevel.getSize().add(diff);
        if (newSize.compareTo(BigDecimal.ZERO) <= 0) {
            // Если в результате размер <= 0, удаляем уровень
            levels.removeFirst();
        } else {
            firstLevel.setSize(newSize);
        }

        logger.info("🎯 Final adjustment applied: total now matches target: {}", totalSize);
        return levels;
    }

    // Убираем старый метод getTakeProfitLevels, он больше не нужен тут
    // public static List<TakeProfitLevel> getTakeProfitLevels(SymbolInfo info, List<TakeProfitLevel> levels) { ... }

    // Убираем compareLevels, если он вызывает перевороты
    // public static List<TakeProfitLevel> compareLevels(List<TakeProfitLevel> levels, String side) { ... }


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
        if (!user.canOpenNewPositoin(signal))
            return OrderResult.error("User: " + user.getTgId() + ", " + user.getTgName() + " already has active position for pair.", "none", signal.getSymbol());

        return OrderResult.ok("Ok", "0", signal.getSymbol());
    }

    public static List<TakeProfitLevel> compareLevels(List<TakeProfitLevel> levels, String side) {
        List<BigDecimal> sizes = new ArrayList<>();
        for (TakeProfitLevel l : levels) {
            sizes.add(l.getSize());
        }

        // Извлекаем все цены и сортируем по возрастанию
        List<BigDecimal> prices = new ArrayList<>();
        for (TakeProfitLevel l : levels) {
            prices.add(l.getPrice());
        }

        prices.sort(Comparator.naturalOrder()); // сортировка по возрастанию

        logger.info("Take adjust side: {}", side);
        if (side.equalsIgnoreCase("SHORT")) {
            prices = prices.reversed();
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
     *
     * @param value   входное число
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
}