package com.plovdev.bot.main;
//1-3

import com.plovdev.bot.bots.EnvReader;
import com.plovdev.bot.modules.beerjes.BitGetTradeService;
import com.plovdev.bot.modules.beerjes.BitUnixTradeService;
import com.plovdev.bot.modules.beerjes.Order;
import com.plovdev.bot.modules.beerjes.bitget.BitGetStopLossTrailer;
import com.plovdev.bot.modules.beerjes.security.BitGetSecurity;
import com.plovdev.bot.modules.beerjes.security.BitUnixSecurity;
import com.plovdev.bot.modules.beerjes.security.EncryptionService;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.models.StopInProfitTrigger;
import com.plovdev.bot.modules.parsers.Signal;

import java.math.BigDecimal;
import java.util.List;

public class TestUtils {
    public static final String BITUNIX_API = "bab1fd89a8b5ff7dd18b61ee2ca85a6a";
    public static final String BITUNIX_SECRET = "ff679d51cd08fed0cd0fc728f9a6098c";
    public static final String BITUNIX_PHRASE = ""; // нету

    public static final String BITGET_API = "bg_cd64c9618a38af76b3765b7f1c24e7a2";
    public static final String BITGET_SECRET = "f57548f3f49c4cd003a7d37fc59162762e1e2e188054267be48dce8ff3cda066";
    public static final String BITGET_PHRASE = "TestBot260825";

    public static final String BITGET_TEST_API = "bg_02d0eb29f6c992a7160ed21ba13f20f4";
    public static final String BITGE_TESTT_SECRET = "7d020a905403ac148a505c34ac60940d4e34dc8314ebc8c676e10befdcaf0a62";
    public static final String BITGET_TEST_PHRASE = "Test123321Test";
    public static final UserEntity bitunixUser = createTestUser("bitunix", BITUNIX_API, BITUNIX_SECRET, BITUNIX_PHRASE);
    public static final UserEntity bitgetUser = createTestUser("bitget", BITGET_API, BITGET_SECRET, BITGET_PHRASE);
    public static final UserEntity bitgetTestUser = createTestUser("bitget", BITGET_TEST_API, BITGE_TESTT_SECRET, BITGET_TEST_PHRASE);
    public static final BitUnixTradeService bitunixService = (BitUnixTradeService) bitunixUser.getUserBeerj();
    public static final BitGetTradeService bitgetService = (BitGetTradeService) bitgetUser.getUserBeerj();
    public static final BitGetSecurity bitgetSecurity = new BitGetSecurity(EnvReader.getEnv("bitgetPassword"));
    public static final BitUnixSecurity bitunixSecurity = new BitUnixSecurity(EnvReader.getEnv("bitunixPassword"));
    public static final Order order = createTestOrder();
    public static final Signal lSignal = createTestSignal("TSTUSDT");
    public static final Signal sSignal = createSTestSignal("ASTERUSDT", bitgetUser);
    public static final BitGetStopLossTrailer trailer = new BitGetStopLossTrailer(bitgetService, StopInProfitTrigger.load("common"));

    public static String scalpLSignal = """
            ℹ️ ADA/USDT
            
            🔴 SHORT
            
            1️⃣🔱 Market
            2️⃣🔱 0.8600
            
            ───────
            
            1️⃣🎯 0.8290
            2️⃣🎯 0.8000
            3️⃣🎯 0.7800
            
            ───────
            
            ⛔️ 0.8700
            
            ───────
            """;

    public static UserEntity createTestUser(String beerj, String api, String secret, String pharse) {
        UserEntity user = new UserEntity();
        user.setBeerj(beerj);

        EncryptionService service = user.getUserBeerj().getSecurityService();

        user.setApiKey(service.encrypt(api));
        user.setSecretKey(service.encrypt(secret));
        user.setPhrase(service.encrypt(pharse));
        user.setUID("12345");
        user.setTgId("7273807801");
        user.setTgName("@Plovchick");
        user.setFirstName("Antony");
        user.setLastName("Plov");
        user.setName("Anton");
        user.setSum("10");
        user.setProc("3");
        user.setWC("0");
        user.setVariant("sum");
        user.setRegVariant("self");
        user.setLanguage("ru");
        user.setPlecho("5");
        user.setStatus("ACTIVE");
        user.setState("none");
        user.setRole("admin");
        user.setReferral("7425474938");
        user.setPositions("100");
        user.setExpiry("unlimited");
        user.setGroup("common");

        return user;
    }

    public static Order createTestOrder() {
        Order order = new Order();
        order.setOrderId("1355887369411788801");
        order.setTradeSide("CLOSE");
        order.setSide("SELL");
        order.setSymbol("DOGEUSDT");
        order.setState("ALIVE");
        order.setcTime("1627293504612");
        order.setuTime("1627293504612");
        order.setFee("");
        order.setOrderType("MARKET");
        order.setPrice(new BigDecimal("34.599"));
        order.setTakeProfitPrice(new BigDecimal("21.50"));
        order.setFilledAmount(new BigDecimal("0.0"));

        order.setTotalProfits("0.272");
        order.setSize(new BigDecimal("200"));
        order.setStopLossPrice(new BigDecimal("35.30"));

        return order;
    }

    public static Signal createTestSignal(String pair) {
        Signal signal = new Signal();
        signal.setDirection("LONG");
        signal.setSymbol(pair);
        signal.setSrc("TG");
        signal.setEntryPrice(null);
        signal.setTypeOreder(List.of("market", "0.1662"));
        signal.addTargets(List.of(new BigDecimal("0.1675"), new BigDecimal("0.1695")));
        signal.setType("tg");
        signal.setStopLoss("0.1640");

        return signal;
    }
    public static Signal createSTestSignal(String pair, UserEntity user) {
        Signal signal = new Signal();
        signal.setDirection("SHORT");
        signal.setSymbol(pair);
        signal.setSrc("TG");
        signal.setEntryPrice(null);
        signal.setTypeOreder(List.of("market"));
        signal.addTargets(List.of(new BigDecimal("124525"), new BigDecimal("125525"), new BigDecimal("123525")));
        signal.setType("tg");
        signal.setStopLoss("1.78");
        return signal;
    }
}