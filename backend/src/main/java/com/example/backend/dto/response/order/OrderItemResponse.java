package com.example.backend.dto.response.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long ticketId,
        String ticketCode,
        String ticketTypeName,
        BigDecimal priceAtPurchase
) {
}
