package com.cyrilsx.russiancases.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RuleUpsertRequest(
        @NotBlank String caseName,
        @NotBlank @Size(max = 255) String titleFr,
        @NotBlank @Size(max = 255) String titleEn,
        @NotBlank @Size(max = 4000) String contentFr,
        @NotBlank @Size(max = 4000) String contentEn
) {
}
