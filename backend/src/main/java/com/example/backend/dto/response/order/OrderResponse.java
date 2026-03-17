package com.example.backend.dto.response.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public record OrderResponse(
        Long id,
        Long customerId,
        BigDecimal totalAmount,
        String status,
        List<OrderItemResponse> items,
        Date createdAt
) {
}
