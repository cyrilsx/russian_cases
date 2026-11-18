package com.cyrilsx.russiancases.web.dto;

public record LevelProgressDto(
        Long levelId,
        String title,
        long total,
        long correct,
        int percent
) {
}
