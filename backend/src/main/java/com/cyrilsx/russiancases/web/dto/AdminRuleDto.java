package com.cyrilsx.russiancases.web.dto;

public record AdminRuleDto(
        Long id,
        String caseName,
        String titleFr,
        String titleEn,
        String contentFr,
        String contentEn
) {
}
