package com.example.backend.dto.response.auth;

import com.example.backend.share.enums.UserRole;

public record AuthResponse(
        Long userId,
        String email,
        String fullName,
        UserRole role,
        boolean active
) {
}
