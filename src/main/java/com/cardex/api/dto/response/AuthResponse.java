package com.cardex.api.dto.response;

import com.cardex.api.enumeration.UserRole;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long id,
        String name,
        String email,
        UserRole role
) {
}