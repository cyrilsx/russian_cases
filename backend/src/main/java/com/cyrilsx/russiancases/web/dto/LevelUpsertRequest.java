package com.cyrilsx.russiancases.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LevelUpsertRequest(
        @NotNull @Min(1) Integer levelNumber,
        @NotBlank @Size(max = 10) String code,
        @NotBlank @Size(max = 255) String titleFr,
        @NotBlank @Size(max = 255) String titleEn,
        @NotBlank @Size(max = 2000) String descriptionFr,
        @NotBlank @Size(max = 2000) String descriptionEn
) {
}
