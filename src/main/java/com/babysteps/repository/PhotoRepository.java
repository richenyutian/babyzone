package com.babysteps.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.babysteps.model.Photo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class PhotoRepository {

    private static final RowMapper<Photo> PHOTO_ROW_MAPPER = (rs, rowNum) -> new Photo(
            rs.getLong("id"),
            rs.getLong("record_id"),
            rs.getString("file_path"),
            rs.getString("file_name")
    );

    private final JdbcTemplate jdbcTemplate;

    public PhotoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long insert(long recordId, String filePath, String fileName) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> createInsertStatement(connection, recordId, filePath, fileName), keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("保存照片失败，未返回主键。");
        }
        return key.longValue();
    }

    public Map<Long, List<Photo>> findByRecordIds(Collection<Long> recordIds) {
        if (recordIds.isEmpty()) {
            return Map.of();
        }
        String placeholders = recordIds.stream().map(id -> "?").collect(Collectors.joining(","));
        List<Photo> photos = jdbcTemplate.query(
                "select id, record_id, file_path, file_name from photos where record_id in (" + placeholders + ") order by id asc",
                PHOTO_ROW_MAPPER,
                recordIds.toArray()
        );
        Map<Long, List<Photo>> grouped = new LinkedHashMap<>();
        for (Photo photo : photos) {
            grouped.computeIfAbsent(photo.recordId(), ignored -> new java.util.ArrayList<>()).add(photo);
        }
        return grouped;
    }

    private PreparedStatement createInsertStatement(Connection connection, long recordId, String filePath,
                                                    String fileName) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("""
                insert into photos (record_id, file_path, file_name)
                values (?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
        statement.setLong(1, recordId);
        statement.setString(2, filePath);
        statement.setString(3, fileName);
        return statement;
    }
}
