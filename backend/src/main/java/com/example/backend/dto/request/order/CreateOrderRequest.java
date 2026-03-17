package com.example.backend.dto.request.order;

import java.util.List;

public record CreateOrderRequest(
        Long customerId,
        List<CreateOrderItemRequest> items
) {
}
