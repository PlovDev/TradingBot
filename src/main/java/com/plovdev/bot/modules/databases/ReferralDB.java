package com.plovdev.bot.modules.databases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReferralDB {
    private final Logger logger = LoggerFactory.getLogger("Bot Base");

    public ReferralDB() {
        createTable();
    }

    public void createTable() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {
            stt.executeUpdate("CREATE TABLE IF NOT EXISTS Referrals (uid TEXT, invited TEXT, positions TEXT)");
        } catch (Exception e) {
            logger.error("Произошла ошибка создания базы данных: {}", e.getMessage());
        }
    }

    public void dropTable() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {
            stt.executeUpdate("DROP TABLE Referrals");
        } catch (Exception e) {
            logger.error("Произошла ошибка удаления базы данных: {}", e.getMessage());
        }
    }

    public void add(String... strings) {
        if (!has(strings[0].trim())) {
            try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
                 PreparedStatement stt = con.prepareStatement("INSERT INTO Referrals (uid, invited, positions) VALUES (?,?,?)")) {
                stt.setString(1, strings[0].trim());
                stt.setString(2, strings[1].trim());
                stt.setString(3, strings[2].trim());

                stt.executeUpdate();
            } catch (Exception e) {
                logger.error("Произошла ошибка добавления нового реферала: {}", e.getMessage());
            }
        }
    }

    public String getByKey(String key, String id) {
        String ret = "";
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT * FROM Referrals WHERE uid = " + id)) {

            while (set.next()) {
                ret = set.getString(key);
            }
        } catch (Exception e) {
            logger.error("Произошла ошибка получения реферала: {}", e.getMessage());
        }
        return ret;
    }

    public void upadateByKey(String key, String val, String id) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("UPDATE Referrals SET " + key + " = ? WHERE uid = ?")) {

            stt.setString(1, val.trim());
            stt.setString(2, id.trim());

            stt.executeUpdate();
        } catch (Exception e) {
            logger.error("Произошла ошибка обновления реферала: {}", e.getMessage());
        }
    }

    public boolean has(String id) {
        List<String> ids = new ArrayList<>();
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT * FROM Referrals")) {

            while (set.next()) {
                ids.add(set.getString("uid"));
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        return ids.contains(id);
    }

    public void remove(String value, String pair) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stat = con.createStatement()) {

            stat.executeUpdate("DELETE FROM Referrals WHERE uid = " + value);
        } catch (Exception e) {
            logger.error("Проижошла ошибка удаления реферала: {}", e.getMessage());
        }
    }
}
