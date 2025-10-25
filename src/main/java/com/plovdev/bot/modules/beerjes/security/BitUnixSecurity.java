package com.plovdev.bot.modules.beerjes.security;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BitUnixSecurity extends BitGetSecurity {
    public BitUnixSecurity(String password) {
        super(password);
    }

    public String generateSign(String nonce, String timestamp, String apiKey,
                                      TreeMap<String, String> queryParamsMap,
                                      String httpBody, String secretKey) {

        StringBuilder queryString = null;
        if (queryParamsMap != null && !queryParamsMap.isEmpty()) {
            queryString = new StringBuilder();
            Set<Map.Entry<String, String>> entrySet = queryParamsMap.entrySet();
            for (Map.Entry<String, String> param : entrySet) {
                if (param.getKey().equals("sign")) {
                    continue;
                }
                String value = param.getValue();

                if (value != null && !value.isEmpty()) {
                    queryString.append(param.getKey());
                    queryString.append(value);
                }
            }
        }
        String baseSignStr = nonce + timestamp + apiKey;
        if (queryString != null) {
            baseSignStr += queryString.toString();
        }
        String digest = SHA256Utils.encrypt(baseSignStr, httpBody);
        return SHA256Utils.encrypt(digest + secretKey);
    }
}