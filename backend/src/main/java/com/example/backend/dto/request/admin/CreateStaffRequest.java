package com.example.backend.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStaffRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email is invalid")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Password confirmation must not be blank")
        String confirmPassword,

        @NotBlank(message = "Full name must not be blank")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @NotBlank(message = "Staff code must not be blank")
        @Size(max = 50, message = "Staff code must not exceed 50 characters")
        String staffCode,

        Long managedEventId
) {
}
