import com.plovdev.bot.modules.beerjes.TakeProfitLevel;
import com.plovdev.bot.modules.beerjes.utils.BeerjUtils;
import com.plovdev.bot.modules.models.SymbolInfo;
import com.plovdev.bot.modules.parsers.Signal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.plovdev.bot.main.TestUtils.bitunixService;
import static com.plovdev.bot.main.TestUtils.bitunixUser;

public class Test {
    public static void main(String[] args) throws Exception {
        testTakes();
    }
    private static void open() throws Exception {
        String symbol = "DOGEUSDT";

        Signal signal = new Signal();
        signal.setPriority(80);
        signal.setSrc("TV");
        signal.setLimitEntryPrice(null);
        signal.setTargets(List.of(new BigDecimal("0.20035"), new BigDecimal("0.2004"), new BigDecimal("0.20045"), new BigDecimal("0.2005")));
        signal.setSymbol(symbol);
        signal.setDirection("LONG");
        signal.setTypeOreder(List.of("market"));
        signal.setEntryPrice(null);
        signal.setStopLoss("0.195");
        signal.setType("tv");

        bitunixService.openOrder(signal, bitunixUser, bitunixService.getSymbolInfo(bitunixUser, symbol), bitunixService.getEntryPrice(symbol));
    }
    private static void testTakes() {
        List<BigDecimal> ratios = List.of(new BigDecimal("60"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"), new BigDecimal("10"));

        String symbol = "DOGEUSDT";
        BigDecimal price = bitunixService.getEntryPrice(symbol);

        Signal signal = new Signal();
        signal.setPriority(80);
        signal.setSrc("TV");
        signal.setLimitEntryPrice(null);
        signal.setTargets(List.of(new BigDecimal("0.20035"), new BigDecimal("0.2004"), new BigDecimal("0.20045"), new BigDecimal("0.2005")));
        signal.setSymbol(symbol);
        signal.setDirection("LONG");
        signal.setTypeOreder(List.of("market"));
        signal.setEntryPrice(null);
        signal.setStopLoss("0.195");
        signal.setType("tv");

        SymbolInfo info = bitunixService.getSymbolInfo(bitunixUser, symbol);
        System.out.println(info);
        BigDecimal posSize = BeerjUtils.getPosSize(bitunixUser, signal, bitunixService, price).multiply(new BigDecimal(bitunixUser.getPlecho())).setScale(info.getVolumePlace(), RoundingMode.HALF_EVEN);
        System.out.println(posSize);

        List<TakeProfitLevel> levels = BeerjUtils.adjustTakeProfits(signal, posSize, ratios, bitunixService.getEntryPrice(symbol), bitunixService.getSymbolInfo(bitunixUser, symbol));
        System.out.println("Levels: " + levels);
        BigDecimal use = BigDecimal.ZERO;
        for (TakeProfitLevel level : levels) {
            use = use.add(level.getSize());
        }
        System.out.println(use);
    }
}