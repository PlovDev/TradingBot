package com.plovdev.bot.modules.databases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plovdev.bot.modules.parsers.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SignalDB {private final Logger logger = LoggerFactory.getLogger("Bot Base");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SignalDB() {
        createTable();
    }

    public void createTable() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {
            stt.executeUpdate("CREATE TABLE IF NOT EXISTS Signal (id TEXT, value TEXT)");
        } catch (Exception e) {
            logger.error("Произошла ошибка создания базы данных сигналов", e);
        }
    }
    public void dropTable() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {
            stt.executeUpdate("DROP TABLE Signal");
        } catch (Exception e) {
            logger.error("Произошла ошибка удаления базы данных сигналов", e);
        }
    }

    public void add(String id, Signal signal) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("INSERT INTO Signal (id, value) VALUES (?,?)")) {
            stt.setString(1, id);
            stt.setString(2, objectMapper.writeValueAsString(signal));

            stt.executeUpdate();
        } catch (Exception e) {
            logger.error("Произошла ошибка добавления сигнала", e);
        }
    }

    public String get(String id) {
        String ret = "";
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("SELECT * FROM Signal WHERE id = ?")) {
            stt.setString(1, id);

            try (ResultSet set = stt.executeQuery()) {
                while (set.next()) {
                    ret = set.getString("value");
                }
            }
        } catch (Exception e) {
            logger.error("Произошла ошибка получения сигнала", e);
        }
        return ret;
    }

    public void upadateByKey(String id, Signal signal) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("UPDATE Signal SET value = ? WHERE id = ?")) {

            stt.setString(1, objectMapper.writeValueAsString(signal));
            stt.setString(2, id.trim());

            stt.executeUpdate();
        } catch (Exception e) {
            logger.error("Произошла ошибка обновления сигнала", e);
        }
    }

    public void remove(String key) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stat = con.createStatement()) {

            stat.executeUpdate("DELETE FROM Signal WHERE id = " + key);
        } catch (Exception e) {
            logger.error("Произошла ошибка удаления сигнала", e);
        }
    }
}