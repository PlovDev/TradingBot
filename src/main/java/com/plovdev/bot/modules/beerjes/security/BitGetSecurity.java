package com.plovdev.bot.modules.beerjes.security;

import com.plovdev.bot.bots.EnvReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

public class BitGetSecurity implements EncryptionService {
    private final Logger logger = LoggerFactory.getLogger("BitGetSecurityService");
    private static final String ALG = "AES/CBC/PKCS5Padding";
    private final SecretKey key;
    private final IvParameterSpec spec;

    public BitGetSecurity(String password) {
        try {
            // Генерируем ключ из пароля
            key = generateKeyFromPassword(password);

            // Фиксированный IV на основе пароля (или генерируем и сохраняем)
            spec = generateIvFromPassword(password);

        } catch (Exception e) {
            logger.error("Ошибка инициализации: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize security service", e);
        }
    }

    private SecretKey generateKeyFromPassword(String password) throws Exception {
        byte[] salt = EnvReader.getEnv("salt").getBytes(); // или генерируйте случайный и сохраняйте
        int iterations = 65536;
        int keyLength = 256;

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private IvParameterSpec generateIvFromPassword(String password) throws Exception {
        // Генерируем IV на основе пароля
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes());
        byte[] iv = Arrays.copyOfRange(hash, 0, 16);
        return new IvParameterSpec(iv);
    }

    @Override
    public String encrypt(String to) {
        try {
            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] encrypted = cipher.doFinal(to.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logger.error("Произошла ошибка шифрования коючей: {}", e.getMessage());
        }
        return to;
    }

    @Override
    public String decrypt(String from) {
        try {
            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] decoded = Base64.getDecoder().decode(from);
            byte[] decrypted = cipher.doFinal(decoded);

            return new String(decrypted);
        } catch (Exception e) {
            logger.error("Произошла ошибка расшифровки ключей: {}", e.getMessage());
        }
        return from;
    }

    @Override
    public void saveKey() {

    }

    @Override
    public String loadKey(String path) {
        return "";
    }

    @Override
    public String generateSignature(String message, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);
            byte[] hash = sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}
