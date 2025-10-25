package com.plovdev.bot.modules.beerjes.bitunix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plovdev.bot.modules.beerjes.Position;
import com.plovdev.bot.modules.beerjes.TradeService;
import com.plovdev.bot.modules.beerjes.utils.BeerjUtils;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.models.OrderResult;
import com.plovdev.bot.modules.models.SettingsService;
import com.plovdev.bot.modules.models.StopInProfitTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class BitUnixStopLossTrailer {
    private final Logger log = LoggerFactory.getLogger("StopLossTrailer");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TradeService service;
    private final SettingsService settings;
    private final StopInProfitTrigger trigger;

    public BitUnixStopLossTrailer(TradeService ts, StopInProfitTrigger t) {
        service = ts;
        settings = new SettingsService();
        trigger = t;
    }


    public OrderResult trailStopByFirstTakeHit(UserEntity user, String symbol, String side, String oId, int pricePlace) {
        log.info("--------------------------------TRALING STOP IN PROFIT----------------------------------");
        BigDecimal ret = trigger.getStopInProfitPercent();

        log.info("Take percent: {}", ret);

        Position position = service.getPositions(user).stream()
                .filter(p -> p.getSymbol().equals(symbol))
                .findFirst()
                .orElse(null);

        if (position != null) {
            return checkByTakeProfit(user, side, oId, position.getEntryPrice(), ret, symbol, pricePlace);
        } else {
            log.warn("No position found for symbol {} to trail stop loss.", symbol);
            return OrderResult.no();
        }
    }

    private OrderResult checkByTakeProfit(UserEntity user, String side, String oId, BigDecimal entry, BigDecimal offsetPercent, String symbol, int pricePlace) {
        try {
            BigDecimal newStop = BeerjUtils.calculateNewStopPrice(side, entry, offsetPercent, pricePlace);
            log.info("Trailing stop activated for order: {}, new stop: {}", oId, newStop);
            return service.updateStopLoss(user, oId, symbol, newStop);
        } catch (Exception e) {
            log.error("Error in checkByTakeProfit for order: {}", oId, e);
            return OrderResult.no();
        }
    }
}