package com.cyrilsx.russiancases.web.dto;

public record AdminLevelDto(
        Long id,
        int levelNumber,
        String code,
        String titleFr,
        String titleEn,
        String descriptionFr,
        String descriptionEn,
        long questionCount
) {
}
