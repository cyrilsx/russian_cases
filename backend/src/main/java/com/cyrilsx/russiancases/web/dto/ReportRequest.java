package com.cyrilsx.russiancases.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportRequest(
        @Min(0) Integer claimedCorrectIndex,
        @NotBlank @Size(max = 2000) String justification
) {
}
