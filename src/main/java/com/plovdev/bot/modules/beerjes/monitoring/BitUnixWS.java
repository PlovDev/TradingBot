package com.plovdev.bot.modules.beerjes.monitoring;

import com.plovdev.bot.bots.EnvReader;
import com.plovdev.bot.listeners.OrderEvent;
import com.plovdev.bot.listeners.PositionEvent;
import com.plovdev.bot.modules.beerjes.BitUnixTradeService;
import com.plovdev.bot.modules.beerjes.Order;
import com.plovdev.bot.modules.beerjes.Position;
import com.plovdev.bot.modules.beerjes.TradeService;
import com.plovdev.bot.modules.beerjes.bitunix.BitunixPrivateWsResponseListener;
import com.plovdev.bot.modules.beerjes.bitunix.FuturesWsPrivateClient;
import com.plovdev.bot.modules.beerjes.bitunix.OrderItem;
import com.plovdev.bot.modules.beerjes.security.BitUnixSecurity;
import com.plovdev.bot.modules.beerjes.utils.BitUnixUtils;
import com.plovdev.bot.modules.databases.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitUnixWS {
    private final Map<String, PositionEvent> events = new HashMap<>();
    private final Map<String, OrderEvent> orders = new HashMap<>();

    private final Map<String, OrderEvent> cancels = new HashMap<>();

    private String stopId;

    public Map<String, PositionEvent> getEvents() {
        return events;
    }

    public Map<String, OrderEvent> getOrders() {
        return orders;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public UserEntity getUser() {
        return user;
    }

    public BitUnixPositionMonitor getMonitor() {
        return monitor;
    }

    public BitUnixSecurity getSecurity() {
        return security;
    }

    private static final Logger log = LoggerFactory.getLogger(BitUnixWS.class);
    private final UserEntity user;
    private final BitUnixPositionMonitor monitor;
    private List<String> symbols;
    private final BitUnixSecurity security = new BitUnixSecurity(EnvReader.getEnv("bitunixPassword"));

    public List<String> getSymbols() {
        return symbols;
    }

    public void setSymbol(List<String> symbols) {
        this.symbols = symbols;
    }

    private final FuturesWsPrivateClient client;

    public void close() {
        client.close();
    }

    public BitUnixWS(UserEntity user, BitUnixTradeService tradeService, String symbol) {
        this.user = user;
        monitor = new BitUnixPositionMonitor(tradeService);
        this.symbols = new ArrayList<>();
        symbols.add(symbol);
        client = new FuturesWsPrivateClient(security.decrypt(user.getApiKey()), security.decrypt(user.getSecretKey()));
    }

    public void addPositionListener(String symbol, PositionEvent event) {
        events.put(symbol, event);
    }

    public void addOrderListener(String symbol, OrderEvent event) {
        orders.put(symbol, event);
    }

    public void addSymbol(String s) {
        symbols.add(s);
    }

    public void removeSymbol(String s) {
        symbols.remove(s);
    }

    public void addTpslListener(String symbol, OrderEvent event) {
        cancels.put(symbol, event);
    }

    public void startMonitoring(String symbol) {
        if (!user.isBituixWebSocketConnected()) {
            client.connect(new BitunixPrivateWsResponseListener() {
                @Override
                public void onOrderChange(String orderResp) {
                    checkOrderFill(orderResp);
                }

                @Override
                public void onTpslChange(String json) {
                    checkTpslFill(json);
                }
            });
        }

        client.subscribe(symbol, "orders");
        client.subscribe(symbol, "tpsl");

//        FuturesWsPublicClient futuresWsPublicClient = new FuturesWsPublicClient();
//        futuresWsPublicClient.connect(new BitunixPublicWsResponseListener() {
//            @Override
//            public void onPrice(PriceResp priceResp) {
//                handleTickerUpdate(priceResp);
//            }
//        });
//        futuresWsPublicClient.subPrice(new PriceSubReq(Collections.singletonList(new PriceSubArg(symbol))));
    }


    // --- В методе checkOrderFill ---
    private void checkOrderFill(String resp) {
        OrderItem item = BitUnixUtils.parseInput(resp);

        // Извлекаем пару из события
        String eventSymbol = item.getSymbol(); // Предполагается, что OrderItem имеет getSymbol()
        if (eventSymbol == null || eventSymbol.isEmpty()) {
            log.warn("Received order update without valid symbol: {}", resp);
            return; // Игнорируем, если нет символа
        }

        TradeService ts = user.getUserBeerj();
        boolean isFill = item.getOrderStatus().toLowerCase().contains("fill");
        boolean isClose = item.isReduceOnly();

        // Уведомляем только подписчиков для этой конкретной пары
        // Для открытия позиции
        if (isFill && !isClose) {
            // Нужно убедиться, что в events есть слушатель для eventSymbol
            PositionEvent event = events.get(eventSymbol);
            if (event != null) { // Проверяем, существует ли слушатель для этой пары
                // Получаем позиции пользователя для этой пары
                List<Position> positions = ts.getPositions(user).stream()
                        .filter(p -> p.getSymbol().equalsIgnoreCase(eventSymbol))
                        .toList();
                if (!positions.isEmpty()) {
                    event.onPositionOpened(positions.getFirst());
                }
            }
        }

        // Уведомляем подписчиков для ордеров
        if (isFill) {
            OrderEvent orderEvent = orders.get(eventSymbol); // Ищем слушатель по паре из события
            if (orderEvent != null) { // Проверяем, существует ли слушатель
                Order input = getOrder(item, isClose, eventSymbol);
                orderEvent.onOrder(input);
            }
        }
    }

    // --- В методе checkTpslFill ---
    private void checkTpslFill(String resp) {
        OrderItem item = BitUnixUtils.parseInputTpsl(resp);

        // Извлекаем пару из события
        String eventSymbol = item.getSymbol(); // Предполагается, что OrderItem имеет getSymbol()
        if (eventSymbol == null || eventSymbol.isEmpty()) {
            log.warn("Received TPSL update without valid symbol: {}", resp);
            return; // Игнорируем, если нет символа
        }

        String status = item.getOrderStatus().toLowerCase();
        boolean isClose = item.isReduceOnly();

        // Уведомляем только подписчиков для этой конкретной пары
        if (status.contains("fill")) {
            OrderEvent cancelEvent = cancels.get(eventSymbol); // Ищем слушатель по паре из события
            if (cancelEvent != null) { // Проверяем, существует ли слушатель
                cancelEvent.onOrder(getOrder(item, isClose, eventSymbol));
            }
        }
    }

    private Order getOrder(OrderItem item, boolean isClose, String symbol) {
        Order input = new Order();
        input.setOrderId(item.getOrderId());
        input.setSymbol(symbol);
        input.setPrice(item.getPrice());
        input.setSize(item.getQty());
        input.setOrderType(item.getType());
        input.setStopTraling(false);
        input.setPosSide((item.getSide().equalsIgnoreCase("buy") ? "LONG" : "SHORT"));
        input.setSide(item.getSide());
        input.setClient0Id(item.getOrderId());
        input.setFilledAmount(item.getQty());
        input.setState(item.getOrderStatus());
        input.setOrderSource("API");
        input.setTradeSide(isClose ? "close" : "open");
        input.setMerginCoin("USDT");
        input.setMarginMode("ISOLATION");
        input.setLeverage(item.getLeverage());
        input.setHoldMode(item.getPositionMode());

        return input;
    }
}