package com.cyrilsx.russiancases.web.dto;

import com.cyrilsx.russiancases.model.ReportStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveReportRequest(
        @NotNull ReportStatus status,
        @Min(0) Integer correctIndex,
        @Size(max = 2000) String note
) {
}
