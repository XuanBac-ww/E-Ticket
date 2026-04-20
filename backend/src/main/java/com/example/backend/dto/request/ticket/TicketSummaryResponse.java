package com.example.backend.dto.request.ticket;

public record TicketSummaryResponse(
        String ticketTypeName,
        Integer totalLimit,
        Integer remainingInField,
        Long actualAvailable,
        Long totalCreated,
        Long totalSold
) {
}
