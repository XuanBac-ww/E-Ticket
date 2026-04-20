package com.example.backend.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
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
