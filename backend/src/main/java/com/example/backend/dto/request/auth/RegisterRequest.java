package com.example.backend.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
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

        @NotBlank(message = "Phone number must not be blank")
        @Pattern(
                regexp = "^(0|\\+84)[0-9]{9,10}$",
                message = "Phone number format is invalid"
        )
        String phoneNumber
) {
}
