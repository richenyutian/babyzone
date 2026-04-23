package com.babysteps.dto;

import java.util.List;

public record HomeResponse(
        String babyName,
        String birthDate,
        long daysSinceBirth,
        String currentAgeLabel,
        List<RecordResponse> records
) {
}
