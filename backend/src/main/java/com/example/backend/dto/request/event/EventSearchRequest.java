package com.example.backend.dto.request.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record EventSearchRequest(
        @NotBlank(message = "Từ khóa Không được để trống") String keyword) {
}
