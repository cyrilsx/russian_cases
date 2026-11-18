package com.cyrilsx.russiancases.web.dto;

public record RuleDto(
        Long id,
        String caseName,
        String caseLabel,
        String title,
        String content
) {
}
