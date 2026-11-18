package com.cyrilsx.russiancases.web.dto;

import java.util.List;

public record QuestionDto(
        Long id,
        Long levelId,
        String caseName,
        String caseLabel,
        String prompt,
        List<String> options,
        String hint
) {
}
