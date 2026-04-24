package com.babysteps.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.babysteps.model.AlbumRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class RecordRepository {

    private static final RowMapper<AlbumRecord> RECORD_ROW_MAPPER = (rs, rowNum) -> new AlbumRecord(
            rs.getLong("id"),
            rs.getString("content"),
            LocalDate.parse(rs.getString("date")),
            rs.getString("tags"),
            LocalDateTime.parse(rs.getString("create_time")),
            LocalDateTime.parse(rs.getString("update_time")),
            List.of()
    );

    private final JdbcTemplate jdbcTemplate;

    public RecordRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insert(String content, LocalDate date, String tags, LocalDateTime now) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> createInsertStatement(connection, content, date, tags, now), keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("创建记录失败，未返回主键。");
        }
        return key.longValue();
    }

    public List<AlbumRecord> findAllDescending() {
        return jdbcTemplate.query("""
                select id, content, date, tags, create_time, update_time
                from records
                order by date desc, create_time desc, id desc
                """, RECORD_ROW_MAPPER);
    }

    private PreparedStatement createInsertStatement(Connection connection, String content, LocalDate date, String tags,
                                                    LocalDateTime now) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                insert into records (content, date, tags, create_time, update_time)
                values (?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, content);
        statement.setString(2, date.toString());
        statement.setString(3, tags);
        statement.setString(4, now.toString());
        statement.setString(5, now.toString());
        return statement;
    }
}
