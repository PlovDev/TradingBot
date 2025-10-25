package com.plovdev.bot.modules.databases;

import com.plovdev.bot.modules.databases.base.PendingRepoistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlanksDB {
    public BlanksDB() {
        createTable();
    }
    private final Logger logger = LoggerFactory.getLogger("Bot Base");

    public void createTable() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {
            stt.executeUpdate("CREATE TABLE IF NOT EXISTS Blanks (id TEXT, uid TEXT, username TEXT, state TEXT, type TEXT, beerj TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void dropTable() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {
            stt.executeUpdate("DROP TABLE Blanks");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(String... strings) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("INSERT INTO Blanks (id, uid, username, state, type, beerj) VALUES (?,?,?,?,?,?)")) {
            stt.setString(1, strings[0].trim());
            stt.setString(2, strings[1].trim());
            stt.setString(3, strings[2].trim());
            stt.setString(4, strings[3].trim());
            stt.setString(5, strings[4].trim());
            stt.setString(6, strings[5].trim());

            stt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getByKey(String key, String id) {
        String ret = "";
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT * FROM Blanks WHERE id = " + id)) {

            while (set.next()) {
                ret = set.getString(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }



    public List<String> getAll() {
        List<String> ret = new ArrayList<>();

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT * FROM Blanks")) {

            while (set.next()) {
                String from = "<b>Новая заявка!<b>\n\n";
                String tgId = "Телеграм ID: <b>" + set.getString("id") + "</b>\n";
                String name = "Имя: <b>" + set.getString("username") + "</b>\n";
                String uid = "UID на бирже: <b>" + set.getString("uid") + "</b>";

                String all = from + tgId + name + uid;
                ret.add(all);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public PendingRepoistory get(String id) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT * FROM Blanks WHERE id = " + id)) {

            while (set.next()) {
                return new PendingRepoistory(
                        set.getString(1),
                        set.getString(2),
                        set.getString(3),
                        set.getString(4),
                        set.getString(5),
                        set.getString(6)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<PendingRepoistory> getAllPendings() {
        List<PendingRepoistory> list = new ArrayList<>();
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT * FROM Blanks")) {

            while (set.next()) {
                list.add(new PendingRepoistory(
                        set.getString(1),
                        set.getString(2),
                        set.getString(3),
                        set.getString(4),
                        set.getString(5),
                        set.getString(6)
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void upadateByKey(String key, String val, String id) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("UPDATE Blanks SET " + key + " = ? WHERE id = ?")) {

            stt.setString(1, val.trim());
            stt.setString(2, id.trim());

            stt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(String value) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stat = con.createStatement()) {

            stat.executeUpdate("DELETE FROM Blanks WHERE id = " + value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}