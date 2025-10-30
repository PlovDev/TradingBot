package com.plovdev.bot.modules.beerjes.bitget.ws;

import com.plovdev.bot.modules.beerjes.Order;
import com.plovdev.bot.modules.beerjes.Position;
import com.plovdev.bot.modules.beerjes.bitget.Account;
import org.json.JSONObject;

public class BitGetWSUtils {
    public static Channel getAction(String json) {
        JSONObject object = new JSONObject(json);
        String action = "unknown";

        if (object.has("event") && !object.isNull("event")) {
            action = object.optString("event", "unknown");
        } else if (object.has("action") && !object.isNull("action")) {
            action = object.optString("action", "unknown");
        } else if (object.has("op") && !object.isNull("op")) {
            action = object.optString("op", "unknown");
        }

        return Channel.valueOf(action.toUpperCase());
    }

    public static Order getOrder(String json) {
        Order order = new Order();

        return order;
    }

    public static Position getPosition(String json) {
        Position position = new Position();

        return position;
    }

    public static WSResult getResult(String json) {
        JSONObject result = new JSONObject(json);
        int code = Integer.parseInt(result.optString("code", "-1"));
        String msg = result.optString("msg", "no msg");
        if (code == 0) {
            return WSResult.successful(msg);
        }
        return WSResult.error(code, msg);
    }

    public static SubscribeResult getSubscribeResult(String json) {
        JSONObject result = new JSONObject(json);
        JSONObject args = result.getJSONObject("arg");

        String instType = args.optString("instType").toUpperCase().replace("-", "_");
        String channel = args.optString("channel", "unknown");
        String instId = args.optString("instId");

        return new SubscribeResult(instId, Channel.of(channel), Type.valueOf(instType), true);
    }

    public static Account getAccount(String json) {
        return new Account();
    }
}