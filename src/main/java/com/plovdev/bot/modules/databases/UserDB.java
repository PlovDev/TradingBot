package com.plovdev.bot.modules.databases;

import com.plovdev.bot.modules.databases.base.Databaseable;
import com.plovdev.bot.modules.databases.base.Entity;
import com.plovdev.bot.modules.exceptions.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

public class UserDB implements Databaseable {
    private final Logger logger = LoggerFactory.getLogger("Bot Base");

    public UserDB() {
        createTable();
    }

    @Override
    public void createTable() {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             Statement stt = con.createStatement()) {

            stt.executeUpdate("CREATE TABLE IF NOT EXISTS Users (tgName TEXT, firstName TEXT," +
                    " lastName TEXT, id TEXT, language TEXT, uid TEXT," +
                    " referral TEXT, state TEXT, wc TEXT, role TEXT," +
                    " apiKey TEXT, secretKey TEXT, phrase TEXT," +
                    " name TEXT, positions TEXT, plecho TEXT, variant TEXT," +
                    " sum TEXT, proc TEXT, beerj TEXT, status TEXT, expiry TEXT, regVar TEXT, grp TEXT, getRew TEXT, invited TEXT, posOpened TEXT, isActiveRef TEXT, activeRefCount TEXT)");
        } catch (Exception e) {
            logger.error("Произошла ошибка создания базы данных: {}", e.getMessage());
        }
    }

    @Override
    public void update(String key, String newVal, String keyVal) {
        updateDef("Users", "id", keyVal, key, newVal);
    }
    public void updateByUid(String key, String newVal, String keyVal) {
        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("UPDATE Users SET " + key + " = ? WHERE uid = ?")) {

            stt.setString(1, newVal);
            stt.setString(2, keyVal);

            stt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Entity get(String id) {
        UserEntity repository = new UserEntity();

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("SELECT * FROM Users WHERE id = ?")) {

            stt.setString(1, id);

            try (ResultSet set = stt.executeQuery()) {
                while (set.next()) {
                    repository = new UserEntity(
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
                }
            }
        } catch (Exception e) {
            logger.error("Произошла ошибка получения пользователя: {}", e.getMessage());
        }
        return repository;
    }



    public Entity getByUid(String id) {
        UserEntity repository = null;

        try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
             PreparedStatement stt = con.prepareStatement("SELECT * FROM Users WHERE uid = ?")) {

            stt.setString(1, id);

            try (ResultSet set = stt.executeQuery()) {
                while (set.next()) {
                    repository = new UserEntity(
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
                }
            }
        } catch (Exception e) {
            logger.error("Произошла ошибка получения пользователя по UID: {}", e.getMessage());
        }
        return repository;
    }

    @Override
    public List<UserEntity> getAll() {
        return getAllDef("Users");
    }

    @Override
    public void remove(String val) {
        removeDef("Users", "id", val);
    }

    @Override
    public void dropTable() {
        dropTableDef("Users");
    }

    @Override
    public boolean has(String id) {
        return hasDef("Users", "id", id);
    }

    @Override
    public void add(UserEntity repository) throws ApiException {
        if (!has(repository.getTgId())) {
            logger.info("Пытаемся добавить нового пользователя...");
            try (Connection con = DriverManager.getConnection("jdbc:sqlite:src/botbase.db");
                 PreparedStatement stt = con.prepareStatement("INSERT INTO Users (tgName, firstName, lastName, id, language, uid, referral, state, wc, role, apiKey, secretKey, phrase, name, positions, plecho, variant, sum, proc, beerj, status, expiry, regVar, grp, getRew, invited, posOpened, isActiveRef, activeRefCount) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {

                stt.setString(1, repository.getTgName().trim());
                stt.setString(2, repository.getFirstName().trim());
                stt.setString(3, repository.getLastName().trim());
                stt.setString(4, repository.getTgId().trim());
                stt.setString(5, repository.getLanguage().trim());
                stt.setString(6, repository.getUID().trim());
                stt.setString(7, repository.getReferral().trim());

                stt.setString(8, repository.getState().trim());
                stt.setString(9, repository.getWC().trim());
                stt.setString(10, repository.getRole().trim());
                stt.setString(11, repository.getApiKey().trim());
                stt.setString(12, repository.getSecretKey().trim());
                stt.setString(13, repository.getPhrase().trim());

                stt.setString(14, repository.getName().trim());
                stt.setString(15, repository.getPositions().trim());
                stt.setString(16, repository.getPlecho().trim());
                stt.setString(17, repository.getVariant().trim());
                stt.setString(18, repository.getSum().trim());
                stt.setString(19, repository.getProc().trim());

                stt.setString(20, repository.getBeerj().trim());
                stt.setString(21, repository.getStatus().trim());
                stt.setString(22, repository.getExpiry().trim());
                stt.setString(23, repository.getRegVariant().trim());
                stt.setString(24, repository.getGroup().trim());

                stt.setString(25, String.valueOf(repository.isGetRew()));
                stt.setString(26, String.valueOf(repository.getInvited()));
                stt.setString(27, String.valueOf(repository.getPosOpened()));
                stt.setString(28, String.valueOf(repository.isActiveRef()));
                stt.setString(29, String.valueOf(repository.getActiveRefCount()));

                stt.executeUpdate();
                logger.info("Пользователь успешно добавлен!");
            } catch (Exception e) {
                logger.error("Произошла ошибка добавления пользователя: {}", e.getMessage());
            }
        } else {
            logger.warn("Данный пользовватель уже существует.");
            throw new ApiException("Данный пользовватель уже существует.");
        }
    }
}