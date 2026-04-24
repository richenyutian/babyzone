package com.babysteps.model;

public record Photo(
        long id,
        long recordId,
        String filePath,
        String fileName
) {
}
