package com.plovdev.bot.modules.databases;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TemplateDB {

    public TemplateDB() {
        createTable();
    }

    public void createTable() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {
            stt.executeUpdate("CREATE TABLE IF NOT EXISTS Templates (name TEXT, en TEXT, ru TEXT, md TEXT)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(String... strings) {
        if (!hasTemplate(strings[0])) {
            try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
                 PreparedStatement stt = con.prepareStatement("INSERT INTO Templates (name, en, ru, md) VALUES (?,?,?,?)")) {
                stt.setString(1, strings[0].trim());
                stt.setString(2, strings[1].trim());
                stt.setString(3, strings[2].trim());
                stt.setString(4, strings[3].trim());

                stt.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            upadateByKey("ru", strings[2], strings[0]);
        }
    }

    private boolean hasTemplate(String id) {
        List<String> ids = new ArrayList<>();
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT * FROM Templates")) {

            while (set.next()) {
                ids.add(set.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids.contains(id);
    }

    public String getByKey(String key, String id) {
        String ret = "";
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT " + key.trim() + " FROM Templates WHERE name = '" + id + "'")) {

            while (set.next()) {
                ret = set.getString(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String get(String key, String val, String getKey) {
        String ret = "";
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT " + getKey + " FROM Templates WHERE " + key + " = " + val)) {

            while (set.next()) {
                ret = set.getString(getKey);
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
             ResultSet set = stt.executeQuery("SELECT * FROM Templates")) {

            while (set.next()) {
                String tgId = "Навзвание: " + set.getString("name") + "\n\n";
                String name = "Русский: " + set.getString("ru") + "\n";
                String ref = "Молдавский: " + set.getString("md") + "\n";
                String uid = "Английский: " + set.getString("en");

                String all = tgId + name + ref + uid;
                ret.add(all);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void upadateByKey(String key, String val, String id) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("UPDATE Templates SET " + key + " = ? WHERE name = ?")) {

            stt.setString(1, val.trim());
            stt.setString(2, id.trim());

            stt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(String key, String value) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stat = con.createStatement()) {

            stat.executeUpdate("DELETE FROM Templates WHERE " + key + " = '" + value + "'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upadate(String key, String val, String updateKey, String updateValue) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("UPDATE Templates SET " + key + " = ? WHERE " + updateKey + " = ?")) {

            stt.setString(1, val.trim());
            stt.setString(2, updateValue.trim());

            stt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
