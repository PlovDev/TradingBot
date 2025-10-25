package com.plovdev.bot.modules.beerjes.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BitGetUtils {
    public static final String BITGET_API_BASE_URL = "https://api.bitget.com";
    public static final String BITGET_API_TEST_URL = "";
    public static final String TEST_ACCOUNT_ENDPOINT_CHECK = "/api/v2/mix/account/accounts";
    public static final String PAIR_LEVERAGE_ENDPOINT_GET = "/api/v2/mix/market/contracts";
    public static final String BALANCE_ENDPOINT = "/api/v2/account/account";
    public static final String PRODUCT_TYPE = "?productType=USDT-FUTURES";
    public static final String GET_POSITIONS_ENDPOINT = "/api/v2/mix/position/all-position?productType=USDT-FUTURES&marginCoin=USDT";
    public static final String GET_HISTORY_POSITIONS_ENDPOINT = "/api/v2/mix/position/history-position?productType=USDT-FUTURES";
    public static final String SYMBOL = "&symbol=";
    public static final String GET_SYMBOLS = "/api/v2/mix/market/tickers?productType=USDT-FUTURES";


    /**
     * Переводит сумму в USDT в количество контрактов для Bitget.
     * @param amount сумма в USDT, которую хочешь вложить в позицию
     * @param price текущая цена актива (например, BTC = 60000)
     * @param lot размер одного контракта (например, 0.001 для BTC/USDT на Bitget)
     * @return количество контрактов, округлённое до 10 знаков
     */
    public static BigDecimal calculateContracts(BigDecimal amount, BigDecimal price, BigDecimal lot) {
        BigDecimal contractPrice = price.multiply(lot); // Сколько USDT стоит 1 контракт
        return amount.divide(contractPrice, 10, RoundingMode.HALF_EVEN); // Сколько контрактов можно купить
    }
}