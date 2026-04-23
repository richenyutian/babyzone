package com.babysteps.dto;

import java.util.List;

public record RecordResponse(
        long id,
        String content,
        String excerpt,
        String date,
        String ageLabel,
        List<String> tags,
        List<PhotoResponse> photos
) {
}
