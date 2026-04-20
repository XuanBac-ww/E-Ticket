package com.example.backend.dto.request.admin;

import jakarta.validation.constraints.NotNull;

public record UpdateUserActiveRequest(
        @NotNull(message = "Active status must not be null")
        Boolean active
) {
}
