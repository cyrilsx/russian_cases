package com.cyrilsx.russiancases.web.dto;

import java.time.Instant;

public record ProgressDto(
        Long questionId,
        String prompt,
        int attempts,
        int correctCount,
        Instant lastAttempt
) {
}
