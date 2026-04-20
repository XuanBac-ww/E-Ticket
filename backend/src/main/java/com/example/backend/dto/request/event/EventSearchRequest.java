package com.example.backend.dto.request.event;

import jakarta.validation.constraints.NotBlank;

public record EventSearchRequest(
        @NotBlank(message = "Keyword must not be blank") String keyword) {
}
