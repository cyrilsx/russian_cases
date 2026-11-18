package com.cyrilsx.russiancases.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SessionAnswerRequest(
        @NotNull Long questionId,
        @NotNull @Min(0) Integer optionIndex
) {
}
