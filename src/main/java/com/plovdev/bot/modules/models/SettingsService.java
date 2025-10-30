package com.plovdev.bot.modules.models;

import com.plovdev.bot.modules.databases.SettingsDB;
import com.plovdev.bot.modules.exceptions.InvalidParametresException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SettingsService {
    private static final Logger log = LoggerFactory.getLogger(SettingsService.class);
    private final SettingsDB settingsDB = new SettingsDB();


    public void setStopInProfitByGroup(String group, String percent, String var) {
        log.info("Updating stop...");
        log.info("Params: group: {}, percent: {}, var: {}", group, percent, var);
        settingsDB.upadateByKey("stop-in-profit", var + ":" + percent, group);
    }

    public String getStopInProfitByGroup(String group) {
        String val = settingsDB.getByKey("stop-in-profit", group);
        return val.substring(val.indexOf(':')+1);
    }
    public String getStopInProfit(String group) {
        return settingsDB.getByKey("stop-in-profit", group);
    }
    public String getStopInProfitVariant(String group) {
        String val = settingsDB.getByKey("stop-in-profit", group);
        return val.substring(0, val.indexOf(":"));
    }

    public List<BigDecimal> getTPRationsByGroup(String group) {
        return parse(settingsDB.getByKey("tp_ratios", group))
                .orElse(List.of(
                        bd("20.0"),
                        bd("20.0"),
                        bd("20.0"),
                        bd("20.0"),
                        bd("20.0")));
    }

    public void setTPRationsByGroup(String goup, List<BigDecimal> list) {
        BigDecimal sum = list.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new InvalidParametresException("Сумма тейков должна быть 100%");
        }

        settingsDB.upadateByKey("tp_ratios", format(list), goup);
    }







    private BigDecimal bd(String val) {
        return new BigDecimal(val);
    }
    private Optional<List<BigDecimal>> parse(String value) {
        if (value == null || value.isEmpty()) return Optional.empty();
        try {
            return Optional.of(Arrays.stream(value.split(","))
                    .map(String::trim)
                    .map(BigDecimal::new)
                    .toList());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public String format(List<BigDecimal> list) {
        return list.stream()
                .map(BigDecimal::toString)
                .collect(Collectors.joining(","));
    }
}