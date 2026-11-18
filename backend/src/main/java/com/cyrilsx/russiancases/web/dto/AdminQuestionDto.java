package com.cyrilsx.russiancases.web.dto;

import java.util.List;

public record AdminQuestionDto(
        Long id,
        Long levelId,
        String caseName,
        String promptFr,
        String promptEn,
        List<String> optionsFr,
        List<String> optionsEn,
        int correctIndex,
        String hintFr,
        String hintEn,
        Long ruleId
) {
}
