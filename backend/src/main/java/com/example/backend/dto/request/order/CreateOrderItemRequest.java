package com.example.backend.dto.request.order;

import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
        @NotNull(message = "ticketId không được để trống")
        Long ticketId
) {
}
