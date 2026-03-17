package com.example.backend.dto.request.order;

public record CreateOrderItemRequest(
        Long ticketTypeId,
        Integer quantity
) {
}
