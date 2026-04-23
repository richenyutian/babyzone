package com.babysteps.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.babysteps.model.Settings;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SettingsRepository {

    private static final RowMapper<Settings> SETTINGS_ROW_MAPPER = SettingsRepository::mapRow;

    private final JdbcTemplate jdbcTemplate;

    public SettingsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Settings> find() {
        return jdbcTemplate.query("""
                select id, baby_name, baby_birthday, admin_username, admin_password_hash, created_time, update_time
                from settings
                where id = 1
                """, SETTINGS_ROW_MAPPER).stream().findFirst();
    }

    public void insert(String babyName, LocalDate babyBirthday, String adminUsername, String passwordHash, LocalDateTime now) {
        jdbcTemplate.update("""
                insert into settings (id, baby_name, baby_birthday, admin_username, admin_password_hash, created_time, update_time)
                values (?, ?, ?, ?, ?, ?, ?)
                """, 1L, babyName, babyBirthday.toString(), adminUsername, passwordHash, now.toString(), now.toString());
    }

    private static Settings mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Settings(
                rs.getLong("id"),
                rs.getString("baby_name"),
                LocalDate.parse(rs.getString("baby_birthday")),
                rs.getString("admin_username"),
                rs.getString("admin_password_hash"),
                LocalDateTime.parse(rs.getString("created_time")),
                LocalDateTime.parse(rs.getString("update_time"))
        );
    }
}
