package com.example.backend.dto.response.ticketType;

import java.math.BigDecimal;

public record TicketTypeResponse(
        Long id,
        Long eventId,
        String name,
        BigDecimal price,
        Integer totalQuantity,
        Integer remainingQuantity
) {
}
