package com.plovdev.bot.modules.databases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SettingsDB {
    private final Logger logger = LoggerFactory.getLogger("Bot Base");

    public SettingsDB() {
        createTable();
    }
    public void createTable() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {
            stt.executeUpdate("CREATE TABLE IF NOT EXISTS Settings (key TEXT, value TEXT, grp TEXT)");
        } catch (Exception e) {
            logger.error("Произошла ошибка создания базы данных настроек", e);
        }
    }
    public void dropTable() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {
            stt.executeUpdate("DROP TABLE Settings");
        } catch (Exception e) {
            logger.error("Произошла ошибка удаления базы данных настроек", e);
        }
    }

    public void add(String key, String value, String group) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("INSERT INTO Settings (key, value, grp) VALUES (?,?,?)")) {
            stt.setString(1, key);
            stt.setString(2, value);
            stt.setString(3, group);

            stt.executeUpdate();
        } catch (Exception e) {
            logger.error("Произошла ошибка добавления настройки", e);
        }
    }

    public String getByKey(String key, String group) {
        String ret = "";
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("SELECT * FROM Settings WHERE key = ? AND grp = ?")) {
            stt.setString(1, key);
            stt.setString(2, group);

            try (ResultSet set = stt.executeQuery()) {
                while (set.next()) {
                    ret = set.getString("value");
                }
            }
        } catch (Exception e) {
            logger.error("Произошла ошибка получения настройки", e);
        }
        return ret;
    }

    public void upadateByKey(String key, String val, String group) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("UPDATE Settings SET value = ? WHERE key = ? AND grp = ?")) {

            stt.setString(1, val.trim());
            stt.setString(2, key.trim());
            stt.setString(3, group);

            stt.executeUpdate();
        } catch (Exception e) {
            logger.error("Произошла ошибка обновления настройки", e);
        }
    }

    public void remove(String key, String group) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stat = con.createStatement()) {

            stat.executeUpdate("DELETE FROM Settings WHERE key = " + key + " AND grp = " + group);
        } catch (Exception e) {
            logger.error("Произошла ошибка удаления настройки", e);
        }
    }
}