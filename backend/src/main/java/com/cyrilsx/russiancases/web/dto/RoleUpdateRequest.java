package com.cyrilsx.russiancases.web.dto;

import com.cyrilsx.russiancases.model.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(
        @NotNull Role role
) {
}
