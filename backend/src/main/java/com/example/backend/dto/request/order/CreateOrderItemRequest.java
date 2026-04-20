package com.example.backend.dto.request.order;

import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
        @NotNull(message = "ticketId must not be null")
        Long ticketId
) {
}
