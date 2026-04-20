package com.example.backend.dto.response.admin;

import com.example.backend.share.enums.UserRole;

import java.util.Date;

public record UserManagementResponse(
        Long id,
        String email,
        String fullName,
        UserRole role,
        boolean active,
        Date createdAt
) {
}
