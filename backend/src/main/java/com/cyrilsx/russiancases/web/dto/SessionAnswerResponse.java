package com.cyrilsx.russiancases.web.dto;

public record SessionAnswerResponse(
        boolean correct,
        int correctIndex,
        String hint,
        String ruleTitle,
        String ruleContent,
        int answered,
        int total
) {
}
