package com.cyrilsx.russiancases.web.dto;

import java.time.Instant;

public record UserDto(
        Long id,
        String username,
        String role,
        Instant createdAt
) {
}
