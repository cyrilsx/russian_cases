package com.cyrilsx.russiancases.web.dto;

public record AuthResponse(
        String token,
        String username
) {
}
