package com.example.backend.dto.response.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long orderId,
        Long ticketId,
        BigDecimal priceAtPurchase
) {
}
