package com.example.backend.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password must not be blank")
        String oldPassword,

        @NotBlank(message = "New password must not be blank")
        @Size(min = 6, message = "New password must be at least 6 characters")
        String newPassword,

        @NotBlank(message = "New password confirmation must not be blank")
        String confirmNewPassword
) {
}
