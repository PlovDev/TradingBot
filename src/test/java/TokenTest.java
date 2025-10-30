import com.plovdev.bot.modules.beerjes.utils.BeerjUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenTest {
    @Test
    public void testBitGetExist() {
        String token = BeerjUtils.getExchangeCoin("SHIBUSDT", "bitget");
        assertEquals("1000SHIBUSDT", token);
    }

    @Test
    public void testBitGetNonExist() {
        String token = BeerjUtils.getExchangeCoin("APTUSDT", "bitget");
        assertEquals("APTUSDT", token);
    }

    @Test
    public void testBitGetExistNon() {
        String token = BeerjUtils.getExchangeCoin("BONKUSDT", "bitget");
        assertEquals("BONKUSDT", token);
    }

    @Test
    public void testBitGetExistAlready() {
        String token = BeerjUtils.getExchangeCoin("1000SHIBUSDT", "bitget");
        assertEquals("1000SHIBUSDT", token);
    }













    @Test
    public void testBitUnixExist() {
        String token = BeerjUtils.getExchangeCoin("BONKUSDT", "bitunix");
        assertEquals("1000BONKUSDT", token);
    }

    @Test
    public void testBitUnixNonExist() {
        String token = BeerjUtils.getExchangeCoin("APTUSDT", "bitunix");
        assertEquals("APTUSDT", token);
    }

    @Test
    public void testBitUnixExistNon() {
        String token = BeerjUtils.getExchangeCoin("SHIBUSDT", "bitunix");
        assertEquals("SHIBUSDT", token);
    }

    @Test
    public void testBitUnixExistAlready() {
        String token = BeerjUtils.getExchangeCoin("1000BONKUSDT", "bitunix");
        assertEquals("1000BONKUSDT", token);
    }
}