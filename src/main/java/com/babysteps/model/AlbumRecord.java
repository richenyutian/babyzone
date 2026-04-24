package com.babysteps.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AlbumRecord(
        long id,
        String content,
        LocalDate date,
        String tags,
        LocalDateTime createTime,
        LocalDateTime updateTime,
        List<Photo> photos
) {
}
