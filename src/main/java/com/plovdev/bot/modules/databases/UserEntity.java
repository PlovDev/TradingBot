package com.plovdev.bot.modules.databases;

import com.plovdev.bot.bots.EnvReader;
import com.plovdev.bot.bots.ExpiryListener;
import com.plovdev.bot.modules.beerjes.*;
import com.plovdev.bot.modules.beerjes.security.BitGetSecurity;
import com.plovdev.bot.modules.beerjes.security.BitUnixSecurity;
import com.plovdev.bot.modules.databases.base.Entity;
import com.plovdev.bot.modules.models.OrderResult;
import com.plovdev.bot.modules.models.TypeValueSwitcher;
import com.plovdev.bot.modules.parsers.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.plovdev.bot.bots.Utils.replaceNull;

public class UserEntity implements Entity {
    private static final Logger log = LoggerFactory.getLogger("UserEntity");
    private boolean isCheckedExpiry = false;

    public boolean isCheckedExpiry() {
        return isCheckedExpiry;
    }

    public void setCheckedExpiry(boolean checkedExpiry) {
        isCheckedExpiry = checkedExpiry;
    }

    private final BitGetTradeService bg = new BitGetTradeService(new BitGetSecurity(EnvReader.getEnv("bitgetPassword")));
    private final BitUnixTradeService bu = new BitUnixTradeService(new BitUnixSecurity(EnvReader.getEnv("bitunixPassword")));
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private String tgName;
    private String firstName;
    private String lastName;
    private String tgId;
    private String language;
    private String UID;
    private String referral;
    private String state;
    private String WC;
    private String role;
    private String apiKey;
    private String secretKey;
    private String phrase;
    private String name;

    private String positions;
    private String plecho;
    private String variant;
    private String sum;
    private String proc;
    private String beerj;
    private String status;
    private String expiry;
    private String regVariant;
    private boolean getRew;
    private int invited;
    private int posOpened;
    private boolean isActiveRef;
    private int activeRefCount;

    public int getActiveRefCount() {
        return activeRefCount;
    }

    public void setActiveRefCount(int activeRefCount) {
        this.activeRefCount = activeRefCount;
    }

    public BitUnixTradeService getBu() {
        return bu;
    }

    public BitGetTradeService getBg() {
        return bg;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public boolean isGetRew() {
        return getRew;
    }

    public void setGetRew(boolean getRew) {
        this.getRew = getRew;
    }

    public int getInvited() {
        return invited;
    }

    public void setInvited(int invited) {
        this.invited = invited;
    }

    public int getPosOpened() {
        return posOpened;
    }

    public void setPosOpened(int posOpened) {
        this.posOpened = posOpened;
    }

    public boolean isActiveRef() {
        return isActiveRef;
    }

    public void setActiveRef(boolean activeRef) {
        isActiveRef = activeRef;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = replaceNull(group, "common");
    }

    private String group;

    public String getRegVariant() {
        return regVariant;
    }

    public void setRegVariant(String regVariant) {
        this.regVariant = replaceNull(regVariant, "self");
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = replaceNull(expiry, "unlimited");
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = replaceNull(status, "UNKNOWN");
    }

    @Override
    public String toString() {
        return String.format("""
                        {
                            "tgName": "%2s",
                            "firstName": "%2s",
                            "lastName": "%2s",
                            "tgId": "%2s",
                            "language": "%2s",
                            "UID": "%2s",
                            "referral": "%2s",
                            "state": "%2s",
                            "wc": "%2s",
                            "role": "%2s",
                            "apiKey": "%2s",
                            "secretKey": "%2s",
                            "phrase": "%2s",
                            "name": "%2s",
                            "positions": "%2s",
                            "plecho": "%2s",
                            "variant": "%2s",
                            "sum": "%2s",
                            "proc": "%2s",
                            "beerj": "%2s",
                            "status": "%2s",
                            "expiry": "%2s",
                            "regVariant": "%2s",
                            "group": "%2s"
                        }
                        """, tgName, firstName, lastName, tgId, language,
                UID, referral, status, WC, role, apiKey, secretKey,
                phrase, name, positions, plecho, variant, sum,
                proc, beerj, status, expiry, regVariant, group
        );
    }

    public UserEntity() {

    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = replaceNull(role, "none");
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = replaceNull(phrase, "none");
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = replaceNull(secretKey, "none");
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = replaceNull(apiKey, "none");
    }

    public String getProc() {
        return proc;
    }

    public void setProc(String proc) {
        this.proc = replaceNull(proc, "none");
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = replaceNull(sum, "none");
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = replaceNull(variant, "none");
    }

    public String getPlecho() {
        return plecho;
    }

    public void setPlecho(String plecho) {
        this.plecho = replaceNull(plecho, "none");
    }

    public String getPositions() {
        return positions;
    }

    public void setPositions(String positions) {
        this.positions = replaceNull(positions, "none");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = replaceNull(name, "none");
    }

    public String getBeerj() {
        return beerj;
    }

    public void setBeerj(String beerj) {
        this.beerj = replaceNull(beerj, "none");
    }

    public TradeService getUserBeerj() {
        if (beerj != null && !beerj.equals("none")) {
            return beerj.equals("bitget") ? bg : bu;
        }
        return null;
    }

    public UserEntity(String... user) {
        tgName = replaceNull(user[0], "none");
        firstName = replaceNull(user[1], "none");
        lastName = replaceNull(user[2], "none");
        tgId = replaceNull(user[3], "none");
        language = replaceNull(user[4], "none");
        UID = replaceNull(user[5], "none");
        referral = replaceNull(user[6], "none");
        state = replaceNull(user[7], "none");
        WC = replaceNull(user[8], "none");
        role = replaceNull(user[9], "none");
        apiKey = replaceNull(user[10], "none");
        secretKey = replaceNull(user[11], "none");
        phrase = replaceNull(user[12], "none");

        name = replaceNull(user[13], "none");
        positions = replaceNull(user[14], "none");
        plecho = replaceNull(user[15], "none");
        variant = replaceNull(user[16], "none");
        sum = replaceNull(user[17], "none");
        proc = replaceNull(user[18], "none");
        beerj = replaceNull(user[19], "none");
        status = replaceNull(user[20], "UNKNOWN");
        expiry = replaceNull(user[21], "unlimited");
        regVariant = replaceNull(user[22], "self");
        group = replaceNull(user[23], "common");
        getRew = Boolean.parseBoolean(replaceNull(user[24], "false"));
        invited = Integer.parseInt(replaceNull(user[25], "0"));
        posOpened = Integer.parseInt(replaceNull(user[26], "0"));
        isActiveRef = Boolean.parseBoolean(replaceNull(user[27], "false"));
        activeRefCount = Integer.parseInt(replaceNull(user[28], "0"));
    }

    public String getWC() {
        return WC;
    }

    public void setWC(String WC) {
        this.WC = replaceNull(WC, "none");
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = replaceNull(state, "none");
    }

    public String getReferral() {
        return referral;
    }

    public void setReferral(String referral) {
        this.referral = replaceNull(referral, "none");
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = replaceNull(UID, "none");
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = replaceNull(language, "none");
    }

    public String getTgId() {
        return tgId;
    }

    public void setTgId(String tgId) {
        this.tgId = replaceNull(tgId, "none");
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = replaceNull(lastName, "none");
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = replaceNull(firstName, "none");
    }

    public String getTgName() {
        return tgName;
    }

    public void setTgName(String tgName) {
        this.tgName = replaceNull(tgName, "none");
    }


    public boolean canOpenNewPositoin(Signal signal) {
        TradeService ts = getUserBeerj();
        String pair = signal.getSymbol();

        log.info("Check user for open order");
        boolean isActive = status.equals("ACTIVE");
        boolean expiried;

        if (expiry.equals("unlimited")) expiried = false;
        else {
            LocalDateTime time = LocalDateTime.parse(expiry, formatter);
            LocalDateTime time1 = LocalDateTime.now();
            expiried = time.isAfter(time1);
        }

        if (!isCheckedExpiry) {
            isCheckedExpiry = true;
            if (expiried) {
                ExpiryListener.notifyListeners(tgId, language);
            }
        }

        log.info("Is active: {}, expired: {}", isActive, expiried);
        if (!isActive || expiried) {
            return false;
        }

        TypeValueSwitcher<Boolean> position = new TypeValueSwitcher<>(true);
        System.out.println("Get positions");
        List<Position> positionsList = ts.getPositions(this).stream().filter(p -> p.getSymbol().equals(pair)).toList();
        positionsList.forEach(e -> position.setT(false));


        TypeValueSwitcher<Boolean> order = new TypeValueSwitcher<>(true);
        System.out.println("Get orders");
        List<Order> orders = ts.getOrders(this).stream().filter(o -> o.getSymbol().equals(pair)).toList();
        orders.stream().filter(e -> e.getTradeSide().equalsIgnoreCase("open")).forEach(e -> {
            if (e.getSymbol().equals(pair)) {
                order.setT(false);
            }
        });

        for (Position p : positionsList) {
            if (p.getSymbol().equals(pair) && !p.getHoldSide().equalsIgnoreCase(signal.getDirection())) {
                try {
                    CompletableFuture<OrderResult> closePos = CompletableFuture.supplyAsync(() -> ts.closePosition(this, p));
                    CompletableFuture<List<OrderResult>> cancelOrders = CompletableFuture.supplyAsync(() -> ts.cancelLimits(this, pair, orders.stream().map(Order::getOrderId).toList()));
                    CompletableFuture.allOf(cancelOrders, cancelOrders).join();

                    log.info("!Equal side position closed: {}", closePos.get());
                    log.info("!Equal side position orders cancel: {}", cancelOrders.get());
                    position.setT(true);
                    order.setT(true);
                } catch (Exception e) {
                    log.error("Closing error: ", e);
                }
                break;
            }
        }
        log.info("Is position? - {}", position.getT());
        log.info("Is order? - {}", order.getT());

        boolean isFalse = position.getT() && order.getT();
        log.info("Total: {}", isFalse);
        return isFalse;
    }
}