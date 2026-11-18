package com.cyrilsx.russiancases.web.dto;

import java.util.List;

public record QuizSessionDto(
        Long id,
        Long levelId,
        int total,
        int answered,
        List<QuestionDto> questions
) {
}
