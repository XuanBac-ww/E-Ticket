package com.example.backend.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateOrderStatusRequest(
        @NotBlank(message = "status must not be blank")
        @Pattern(
                regexp = "PENDING|PAID|CANCELLED",
                message = "status must be one of PENDING, PAID, or CANCELLED"
        )
        String status
) {
}
