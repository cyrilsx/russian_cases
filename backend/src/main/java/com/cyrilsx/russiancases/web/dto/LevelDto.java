package com.cyrilsx.russiancases.web.dto;

public record LevelDto(
        Long id,
        int levelNumber,
        String code,
        String title,
        String description,
        long questionCount
) {
}
