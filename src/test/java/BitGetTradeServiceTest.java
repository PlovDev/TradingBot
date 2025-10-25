import com.plovdev.bot.main.TestUtils;
import com.plovdev.bot.modules.beerjes.Order;
import com.plovdev.bot.modules.beerjes.security.EncryptionService;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.parsers.Signal;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BitGetTradeServiceTest {
    @Test
    public void ordersCheck() {
        assertFalse(TestUtils.bitgetService.getOrders(TestUtils.bitgetUser).isEmpty());
    }

    @Test
    public void balanceCheck() throws Exception {
        assertTrue(TestUtils.bitgetService.getBalance(TestUtils.bitgetUser).compareTo(new BigDecimal("1000")) > 0);
    }
    @Test
    public void openOrderTest() throws Exception {
        TestUtils.bitgetService.openOrder(createTestSignal("DOGEUSDT"), TestUtils.bitgetUser, TestUtils.bitgetService.getSymbolInfo(TestUtils.bitgetUser, "DOGEUSDT"), TestUtils.bitgetService.getEntryPrice("DOGEUSDT"));
    }


    private static UserEntity createTestUser(String beerj, String api, String secret, String pharse) {
        UserEntity user = new UserEntity();
        user.setBeerj(beerj);

        EncryptionService service = user.getUserBeerj().getSecurityService();

        user.setApiKey(service.encrypt(api));
        user.setSecretKey(service.encrypt(secret));
        user.setPhrase(service.encrypt(pharse));
        user.setUID("12345");
        user.setTgId("7273807801");
        user.setTgName("@Vcusni_Plovchick");
        user.setFirstName("Antony");
        user.setLastName("Plov");
        user.setName("Anton");
        user.setSum("10");
        user.setProc("1");
        user.setWC("0");
        user.setVariant("sum");
        user.setRegVariant("self");
        user.setLanguage("ru");
        user.setPlecho("5");
        user.setStatus("ACTIVE");
        user.setState("none");
        user.setRole("user");
        user.setReferral("7425474938");
        user.setPositions("100");
        user.setExpiry("unlimited");
        user.setGroup("common");

        return user;
    }

    private static Order createTestOrder() {
        Order order = new Order();
        order.setOrderId("1350516849007230977");
        order.setTradeSide("OPEN");
        order.setSide("BUY");
        order.setSymbol("BTCUSDT");
        order.setState("ALIVE");
        order.setcTime("1627293504612");
        order.setuTime("1627293504612");
        order.setFee("");
        order.setClient0Id("777777");
        order.setFilledAmount(new BigDecimal("0.0"));

        order.setTotalProfits("0.272");
        order.setSize(new BigDecimal("200"));
        order.setStopLossPrice(new BigDecimal("0.202"));

        return order;
    }

    private static Signal createTestSignal(String pair) {
        Signal signal = new Signal();
        signal.setDirection("LONG");
        signal.setSymbol(pair);
        signal.setSrc("TG");
        signal.setEntryPrice(null);
        signal.setTypeOreder(List.of("market"));
        signal.addTargets(List.of(new BigDecimal("0.19"), new BigDecimal("0.2")));
        signal.setType("tg");
        signal.setStopLoss("0.17");

        return signal;
    }
}