package com.example.backend.dto.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "Ticket list must not be empty")
        @Valid
        List<CreateOrderItemRequest> items
) {
}
