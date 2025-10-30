package com.plovdev.bot.modules.beerjes.bitunix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plovdev.bot.modules.beerjes.*;
import com.plovdev.bot.modules.beerjes.monitoring.BitUnixWS;
import com.plovdev.bot.modules.beerjes.utils.BeerjUtils;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.models.*;
import com.plovdev.bot.modules.parsers.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class BitUnixTakesSetuper {
    private final Logger logger = LoggerFactory.getLogger("TakeSetuper");
    private final com.plovdev.bot.modules.logging.Logger custom = new com.plovdev.bot.modules.logging.Logger();
    private final BitUnixTradeService service;
    private final SettingsService settings = new SettingsService();
    private final BitUnixStopLossTrailer trailer;
    private final StopInProfitTrigger trigger;
    private final ObjectMapper mapper = new ObjectMapper();

    public BitUnixTakesSetuper(StopInProfitTrigger tgr, BitUnixTradeService service, BitUnixStopLossTrailer st) {
        this.service = service;
        trailer = st;
        trigger = tgr;
    }

    public void manageTakesInMonitor(BitUnixWS ws, String symbol, UserEntity user, List<OrderResult> ids, String stopLossId, List<TakeProfitLevel> tpLevels, SymbolInfo info, String direction, Signal signal, OrderExecutionContext context) {
        TypeValueSwitcher<Boolean> isOrdered = new TypeValueSwitcher<>(false);

        TakeProfitLevel firstLlevel = tpLevels.getFirst();
        ws.addOrderListener(symbol, inputOrder -> {
            String side = inputOrder.getTradeSide();
            String orderId = inputOrder.getOrderId();

            if (side.equalsIgnoreCase("open")) {
                if (inputOrder.getOrderType().equalsIgnoreCase("limit")) {
                    // Минимальный сон (100-500мс), чтобы дать API закончить транзакцию. 1000мс было слишком долго.
                    try {
                        Thread.sleep(1000);
                        List<TakeProfitLevel> newLevels = tpLevels;

                        // ---------- 1. БЕЗОПАСНЫЙ ПОИСК ПОЗИЦИИ ----------
                        List<Position> foundPositions = service.getPositions(user).stream().filter(p -> {
                            String symb = p.getSymbol();
                            boolean isSymb = symb.equals(symbol);
                            String hs = p.getHoldSide().toLowerCase();
                            boolean isLong = hs.equals("buy") || hs.equals("long");
                            String directionMatch = isLong ? "LONG" : "SHORT";
                            return isSymb && directionMatch.equalsIgnoreCase(direction);
                        }).toList();

                        if (foundPositions.isEmpty()) {
                            logger.warn("Position not found after open event for symbol: {}. Skipping TP adjustment.", symbol);
                            return;
                        }

                        Position position = foundPositions.getFirst();

                        // ---------- 2. БЕЗОПАСНОЕ ИЗВЛЕЧЕНИЕ РАЗМЕРА ----------
                        BigDecimal totalSize = position.getTotal();

                        if (totalSize == null) {
                            logger.warn("Position total size is null. Cannot adjust takes.");
                            return;
                        }

                        // ---------- 3. ПЕРЕСЧЕТ ОБЪЕМОВ (с гарантией точности из BeerjUtils) ----------
                        // Этот вызов вернет список newTakes, сумма объемов которого ТОЧНО равна totalSize
                        List<TakeProfitLevel> newTakes = BeerjUtils.reAdjustTakeProfits(totalSize, newLevels, info, service.getEntryPrice(symbol), inputOrder.getPosSide());

                        // ---------- 4. МОДИФИКАЦИЯ ОРДЕРОВ С СОРТИРОВКОЙ ----------
                        // Получаем открытые тейк-профиты
                        List<Order> orderList = service.getOrders(user).stream()
                                .filter(o -> o.getSymbol().equals(symbol) && o.isReduceOnly())
                                // КРИТИЧНО: Сортируем ордера по цене, чтобы гарантировать совпадение с newTakes
                                .sorted(Comparator.comparing(Order::getPrice))
                                .toList();
                        orderList.forEach(o -> {
                            service.closeOrder(user, o);
                        });

                        newLevels = BeerjUtils.adjustTakeProfits(signal, totalSize, settings.getTPRationsByGroup(user.getGroup()), service.getEntryPrice(signal.getSymbol()), info);
                        if (newLevels.size() - 1 <= trigger.getTakeToTrailNumber()) {
                            trigger.setTakeToTrailNumber(Math.max(newLevels.size() - 2, 0));
                        }

                        // 7. Выставляем тейки
                        List<Map<String, String>> orders = new ArrayList<>();
                        for (TakeProfitLevel level : newLevels) {
                            try {
                                Map<String, String> payload = new HashMap<>();
                                payload.put("qty", level.getSize().toPlainString());
                                payload.put("price", level.getPrice().toPlainString());
                                payload.put("side", signal.getDirection().equalsIgnoreCase("long") ? "BUY" : "SELL");
                                payload.put("tradeSide", "CLOSE");
                                payload.put("positionId", position.getPosId());
                                payload.put("orderType", "LIMIT");
                                payload.put("effect", "GTC");
                                payload.put("reduceOnly", "true");


                                payload.put("tpPrice", level.getPrice().toPlainString()); // Цена активации
                                payload.put("tpOrderPrice", level.getPrice().toPlainString()); // Цена исполнения
                                payload.put("tpOrderType", "MARKET");
                                payload.put("tpStopType", "MARK_PRICE");

                                System.out.println("\n\n");
                                custom.blue("Payload:");
                                custom.warn(mapper.writeValueAsString(payload));
                                System.out.println("\n\n");

                                orders.add(payload);
                            } catch (Exception e) {
                                logger.warn("Failed to place TP at {}: {}", level.getPrice(), e.getMessage());
                            }
                        }
                        List<OrderResult> results = service.placeOrders(user, symbol, orders);
                        for (int i = 0; i < results.size(); i++) {
                            ids.set(i, results.get(i));
                        }
                    } catch (Exception e) {
                        System.out.println("Sleep error");
                    }
                }
            }


            if (orderId.equals(stopLossId)) {
                logger.info("Stop-loss(id: {}) is hit!", orderId);

                List<Order> ordersList = service.getOrders(user);
                List<Order> toCancel = new ArrayList<>();

                for (Order order : ordersList) {
                    if (order.getSymbol().equals(symbol)) {
                        toCancel.add(order);
                    }
                }

                custom.info("Orders to cancel: {}", toCancel);
                service.cancelLimits(user, symbol, toCancel.stream().map(Order::getOrderId).toList());
            }
            System.out.println(isOrdered.getT() + " - isOrdered");

            if (side.equalsIgnoreCase("close")) {
                context.setExecutedTakeNumber(context.getExecutedTakeNumber()+1);
                if (!isOrdered.getT()) {
                    if (isTakeHit(inputOrder, tpLevels, ids, symbol, context)) {
                        try {
                            try {
                                logger.info("Fisrt take-profit(id: {}, symbol: {}) is hit!", orderId, side);
                                List<Order> ordersList = service.getOrders(user).stream().filter(order -> {
                                    boolean isReduce = order.isReduceOnly();
                                    String s = order.getSymbol();
                                    logger.info("Is reduce: {}, symbol: {}", isReduce, symbol);
                                    return s.equals(symbol) && !isReduce;
                                }).toList();

                                logger.info("Orders to cancel: {}", ordersList);
                                for (Order order : ordersList) {
                                    service.closeOrder(user, order);
                                }
                            } catch (Exception e) {
                                logger.error("Err: ", e);
                            }
                            //---------------------------------------LIMITS CANCELED------------------------------------------\\
                            if (trigger.isTakeVariant()) {
                                System.out.println("STOP LOSS before trailing: " + stopLossId);
                                OrderResult stopOrder = trailer.trailStopByFirstTakeHit(user, symbol, side, stopLossId, info.getPricePlace());
                                if (stopOrder.succes()) {
                                    //ws.close();
                                    logger.info("Stop loss for: {} was trailing success", stopOrder);
                                } else {
                                    isOrdered.setT(false);
                                    trigger.setTakeToTrailNumber(trigger.getTakeToTrailNumber() + 1);
                                    BigDecimal oldPercent = trigger.getStopInProfitPercent();
                                    trigger.setStopInProfitPercent(new BigDecimal("-0.5"));
                                    try {
                                        OrderResult stopOrderAgain = trailer.trailStopByFirstTakeHit(user, symbol, side, stopLossId, info.getPricePlace());
                                        System.out.println("Again place stop order: " + stopOrderAgain);
                                    } catch (Exception e) {
                                        logger.error("Cann't trailing stop in profit to enter-point.");
                                    } finally {
                                        trigger.setStopInProfitPercent(oldPercent);
                                    }
                                }
                            }
                            isOrdered.setT(true);
                        } catch (Exception e) {
                            logger.info("Fisrt TP hit error: ", e);
                        }
                    }
                }
            }
        });
    }

    private boolean isTakeHit(Order inputOrder, List<TakeProfitLevel> tpLevels, List<OrderResult> ids, String symbol, OrderExecutionContext context) {
        logger.info("Data params to calc, is first take hit for symvol {}?", symbol);
        tpLevels.sort(Comparator.comparing(TakeProfitLevel::getPrice));
        String posSide = inputOrder.getPosSide();

        logger.info("Order side: {}, Tp levels: {} ids: {}", posSide, tpLevels, ids);
        if (posSide.equalsIgnoreCase("long") || posSide.equalsIgnoreCase("buy")) {
            tpLevels = tpLevels.reversed();
            ids = ids.reversed();
        }

        TakeProfitLevel level = tpLevels.get(trigger.getTakeToTrailNumber());
        logger.info("First level is: {}", level);

        boolean isId = false;
        if (ids.size() >= trigger.getTakeToTrailNumber()) {
            isId = inputOrder.getOrderId().equals(ids.get(trigger.getTakeToTrailNumber()).id());
            logger.info("Order id: {}, first id: {}, Is id? - {}", inputOrder.getOrderId(), ids.get(trigger.getTakeToTrailNumber()).id(), isId);
        }
        BigDecimal priceDifference = inputOrder.getPrice().subtract(level.getPrice()).abs(); // Разница между ценами
        BigDecimal allowedPercentage = new BigDecimal("0.0001");
        BigDecimal allowedDifference = level.getPrice().multiply(allowedPercentage).setScale(10, RoundingMode.HALF_EVEN);
        boolean isPrice = priceDifference.compareTo(allowedDifference) <= 0;

        boolean isClose = inputOrder.getTradeSide().equalsIgnoreCase("close");

        logger.info("Input order price: {}, first price: {}, Is price? - {}", inputOrder.getPrice(), level.getPrice(), isPrice);
        logger.info("Input order trade side: {}, first trade side: close - must, Is TS? - {}", inputOrder.getTradeSide(), isClose);

        int execTake = context.getExecutedTakeNumber();
        int settingsTake = trigger.getTakeToTrailNumber();

        logger.info("Executed take number: {}, settings take number for trailing stop: {}", execTake, settingsTake);

        boolean finalResult = isId || (isPrice && isClose) || execTake == settingsTake;
        logger.info("Final result, is first tp? - {}", finalResult);

        return finalResult;
    }
}