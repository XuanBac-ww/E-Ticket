package com.example.backend.dto.response.jwt;

import com.example.backend.share.enums.UserRole;

public record JwtClaims(
        Long userId,
        String email,
        UserRole role,
        String type
) {
}
