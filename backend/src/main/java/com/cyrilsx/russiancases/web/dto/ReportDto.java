package com.cyrilsx.russiancases.web.dto;

import com.cyrilsx.russiancases.model.ReportStatus;

import java.time.Instant;

public record ReportDto(
        Long id,
        Long questionId,
        String userId,
        Integer claimedCorrectIndex,
        String justification,
        ReportStatus status,
        Instant createdAt,
        String resolvedBy,
        Instant resolvedAt,
        String resolutionNote
) {
}
