package com.babysteps.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Settings(
        long id,
        String babyName,
        LocalDate babyBirthday,
        String adminUsername,
        String adminPasswordHash,
        LocalDateTime createdTime,
        LocalDateTime updateTime
) {
}
