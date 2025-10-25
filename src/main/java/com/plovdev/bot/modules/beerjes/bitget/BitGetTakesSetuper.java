package com.plovdev.bot.modules.beerjes.bitget;

import com.plovdev.bot.modules.beerjes.BitGetTradeService;
import com.plovdev.bot.modules.beerjes.Order;
import com.plovdev.bot.modules.beerjes.Position;
import com.plovdev.bot.modules.beerjes.TakeProfitLevel;
import com.plovdev.bot.modules.beerjes.monitoring.BitGetWS;
import com.plovdev.bot.modules.beerjes.utils.BeerjUtils;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

public class BitGetTakesSetuper {
    private final Logger logger = LoggerFactory.getLogger("TakeSetuper");
    private final com.plovdev.bot.modules.logging.Logger custom = new com.plovdev.bot.modules.logging.Logger();
    private final BitGetTradeService service;
    private final SettingsService settings = new SettingsService();
    private final BitGetStopLossTrailer trailer;
    private final StopInProfitTrigger trigger;

    public BitGetTakesSetuper(StopInProfitTrigger trg, BitGetTradeService service, BitGetStopLossTrailer st) {
        this.service = service;
        trailer = st;
        trigger = trg;
    }


    public List<Map<String, String>> placeTakes(BigDecimal positionSize, List<TakeProfitLevel> tpLevels, String symbol, String direction) {
        System.out.println(positionSize + " - Position size");
        List<Map<String, String>> orders = new ArrayList<>();
        for (TakeProfitLevel level : tpLevels) {
            System.out.println(level);
            BigDecimal size = level.getSize();

            Map<String, String> payload = new HashMap<>();
            payload.put("symbol", symbol); // Добавляем суффикс для фьючерсов!
            payload.put("productType", "USDT-FUTURES"); // В ВЕРХНЕМ РЕГИСТРЕ!
            payload.put("marginMode", "isolated"); // Обязательный параметр
            payload.put("marginCoin", "USDT"); // В ВЕРХНЕМ РЕГИСТРЕ!
            payload.put("size", size.toPlainString());
            payload.put("side", direction.equalsIgnoreCase("long") ? "BUY" : "SELL");
            payload.put("tradeSide", "close");
            payload.put("orderType", "limit");
            payload.put("price", level.getPrice().toPlainString());
            payload.put("force", "gtc"); // Обязательно для лимитных ордеров!
            payload.put("reduceOnly", "yes"); // КРИТИЧЕСКИ ВАЖНО - только закрытие!
            orders.add(payload);
        }
        return orders;
    }

    public void manageTakesInMonitor(BitGetWS ws, String symbol, UserEntity user, List<Map<String, String>> orders, List<TakeProfitLevel> tpLevels, SymbolInfo info, String direction, OrderExecutionContext context) {
        context.setCurrentTakeProfitIds(
                service.placeOrders(user, symbol, orders).stream()
                        .filter(OrderResult::succes)
                        .toList()
        );

        ws.addOrderListener(symbol, inputOrder -> {
            String tradeSide = inputOrder.getTradeSide();
            String orderId = inputOrder.getOrderId();

            if (tradeSide.equalsIgnoreCase("open")) {
                List<String> tpIdsToCancel = context.getCurrentTakeProfitIds().stream().map(OrderResult::id).toList();
                service.cancelLimits(user, symbol, tpIdsToCancel);
                service.cancelStopLoss(user, context.getStopLossId(), symbol);

                Position updatedPosition = service.getPositions(user).stream()
                        .filter(p -> p.getSymbol().equals(symbol) && p.getHoldSide().equalsIgnoreCase(direction))
                        .findFirst().orElse(null);

                if (updatedPosition != null) {
                    OrderResult newSlResult = service.placeStopLoss(user, updatedPosition, String.valueOf(inputOrder.getPresetStopLossPrice()), info, context);
                    context.setStopLossId(newSlResult.id());

                    List<TakeProfitLevel> newTakes = BeerjUtils.reAdjustTakeProfits(updatedPosition.getTotal(), tpLevels, info, updatedPosition.getEntryPrice(), direction);
                    List<Map<String, String>> newTakesPayload = placeTakes(updatedPosition.getTotal(), newTakes, symbol, direction);
                    context.setCurrentTakeProfitIds(
                            service.placeOrders(user, symbol, newTakesPayload).stream()
                                    .filter(OrderResult::succes)
                                    .toList()
                    );
                }
            }

            if (isTakeHit(inputOrder, tpLevels, context.getCurrentTakeProfitIds())) {
                context.incrementTakeProfitLevelsHit();
                if (context.getTakeProfitLevelsHit() == 1) { // трейлинг только после первого тейка
                    try {
                        logger.info("First take-profit(id: {}) is hit!", orderId);
                        service.getOrders(user).stream()
                                .filter(o -> o.getSymbol().equals(symbol) && o.getTradeSide().equalsIgnoreCase("open"))
                                .forEach(orderToCancel -> {
                                    try {
                                        service.closeOrder(user, orderToCancel);
                                    } catch (Exception e) {
                                        logger.error("Failed to cancel limit order: {}", orderToCancel.getOrderId(), e);
                                    }
                                });

                        if (trigger.isTakeVariant()) {
                            OrderResult stopOrder = trailer.trailStopByFirstTakeHit(user, symbol, inputOrder.getPosSide(), context.getStopLossId(), info.getPricePlace());
                            context.setStopLossId(stopOrder.id());
                        }
                    } catch (Exception e) {
                        logger.error("First TP hit error: ", e);
                    }
                }
            }
        });
    }
    private boolean isTakeHit(Order inputOrder, List<TakeProfitLevel> tpLevels, List<OrderResult> ids) {
        logger.info("Data params to calc, is first take hit?");
        tpLevels.sort(Comparator.comparing(TakeProfitLevel::getPrice));
        String posSide = inputOrder.getPosSide();

        logger.info("Order side: {}, Tp levels: {} ids: {}", posSide, tpLevels, ids);
        if (posSide.equalsIgnoreCase("short") || posSide.equalsIgnoreCase("sell")) {
            tpLevels = tpLevels.reversed();
            ids = ids.reversed();
        }
        TakeProfitLevel level = tpLevels.get(trigger.getTakeToTrailNumber());
        logger.info("First level is: {}", level);


        boolean isId = false;
        if (!ids.isEmpty()) {
            isId = inputOrder.getOrderId().equals(ids.get(trigger.getTakeToTrailNumber()).id());
        }
        boolean isPrice = (inputOrder.getPrice().compareTo(level.getPrice()) == 0);
        boolean isClose = inputOrder.getTradeSide().equalsIgnoreCase("close");

        logger.info("Order id: {}, first id: {}, Is id? - {}", inputOrder.getOrderId(), ids.get(trigger.getTakeToTrailNumber()).id(), isId);

        logger.info("Input order price: {}, first price: {}, Is price? - {}", inputOrder.getPrice(), level.getPrice(), isPrice);
        logger.info("Input order trade side: {}, first trade side: close - must, Is TS? - {}", inputOrder.getTradeSide(), isClose);

        boolean finalResult = isId || (isPrice && isClose);
        logger.info("Final result, is first tp? - {}", finalResult);

        return finalResult;
    }
}