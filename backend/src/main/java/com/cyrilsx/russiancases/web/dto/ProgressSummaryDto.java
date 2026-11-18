package com.cyrilsx.russiancases.web.dto;

import java.util.List;

public record ProgressSummaryDto(
        String userId,
        long totalAttempts,
        long totalCorrect,
        int percent,
        List<LevelProgressDto> levels
) {
}
