package com.cyrilsx.russiancases.web.dto;

import java.time.Instant;

public record AdminProgressDto(
        String userId,
        Long questionId,
        int attempts,
        int correctCount,
        Instant lastAttempt
) {
}
