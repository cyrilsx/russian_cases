package com.cyrilsx.russiancases.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionUpsertRequest(
        @NotNull Long levelId,
        @NotBlank String caseName,
        @NotBlank @Size(max = 2000) String promptFr,
        @NotBlank @Size(max = 2000) String promptEn,
        @NotEmpty @Size(min = 2, max = 10) List<@NotBlank @Size(max = 500) String> optionsFr,
        @NotEmpty @Size(min = 2, max = 10) List<@NotBlank @Size(max = 500) String> optionsEn,
        @NotNull @Min(0) Integer correctIndex,
        @NotBlank @Size(max = 2000) String hintFr,
        @NotBlank @Size(max = 2000) String hintEn,
        Long ruleId
) {
}
