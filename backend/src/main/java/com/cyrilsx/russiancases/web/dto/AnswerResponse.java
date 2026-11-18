package com.cyrilsx.russiancases.web.dto;

public record AnswerResponse(
        boolean correct,
        int correctIndex,
        String hint,
        String ruleTitle,
        String ruleContent
) {
}
