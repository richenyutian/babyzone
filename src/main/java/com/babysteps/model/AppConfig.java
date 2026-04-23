package com.babysteps.model;

import java.time.LocalDate;

public record AppConfig(
        String babyName,
        LocalDate babyBirthday,
        String adminUsername,
        String adminPassword
) {
}
