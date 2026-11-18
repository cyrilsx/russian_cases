package com.cyrilsx.russiancases.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotBlank String userId,
        @NotNull @Min(0) Integer optionIndex
) {
}
