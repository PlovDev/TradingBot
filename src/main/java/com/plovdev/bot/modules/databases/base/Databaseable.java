package com.plovdev.bot.modules.databases.base;

import com.plovdev.bot.modules.databases.UserEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public interface Databaseable {
    void createTable();
    void update(String key, String newVal, String keyVal);
    Entity get(String id);
    List<UserEntity> getAll();
    void remove(String val);
    void dropTable();
    boolean has(String id);

    default void updateDef(String table, String key, String keyVal, String update, String newVal) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("UPDATE " + table + " SET " + update + " = ? WHERE id = ?")) {

            stt.setString(1, newVal);
            stt.setString(2, keyVal);

            stt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    default List<UserEntity> getAllDef(String table) {
        List<UserEntity> ret = new ArrayList<>();

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT * FROM " + table)) {

            while (set.next()) {
                UserEntity repository = new UserEntity(
                        set.getString(1),
                        set.getString(2),
                        set.getString(3),
                        set.getString(4),
                        set.getString(5),
                        set.getString(6),
                        set.getString(7),
                        set.getString(8),
                        set.getString(9),
                        set.getString(10),
                        set.getString(11),
                        set.getString(12),
                        set.getString(13),

                        set.getString(14),
                        set.getString(15),
                        set.getString(16),
                        set.getString(17),
                        set.getString(18),
                        set.getString(19),
                        set.getString(20),
                        set.getString(21),
                        set.getString(22),
                        set.getString(23),
                        set.getString(24),

                        set.getString(25),
                        set.getString(26),
                        set.getString(27),
                        set.getString(28),
                        set.getString(29)
                );

                ret.add(repository);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    default void removeDef(String table, String key, String val) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stat = con.prepareStatement("DELETE FROM " + table + " WHERE id = ?")) {

            stat.setString(1, val);
            stat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    default void dropTableDef(String table) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {

            stt.executeUpdate("DROP TABLE " + table);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void add(UserEntity repository);
    default boolean hasDef(String table, String id, String key) {
        List<String> ids = new ArrayList<>();
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement();
             ResultSet set = stt.executeQuery("SELECT * FROM " + table)) {

            while (set.next()) {
                ids.add(set.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids.contains(key);
    }
}
