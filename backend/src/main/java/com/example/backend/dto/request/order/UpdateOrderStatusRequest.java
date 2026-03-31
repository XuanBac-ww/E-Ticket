package com.example.backend.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOrderStatusRequest(
        @NotBlank(message = "status không được để trống")
        @Pattern(
                regexp = "PENDING|PAID|CANCELLED",
                message = "status chỉ được là PENDING, PAID hoặc CANCELLED"
        )
        String status
) {
}