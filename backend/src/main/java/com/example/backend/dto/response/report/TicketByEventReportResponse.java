package com.example.backend.dto.response.report;

public record TicketByEventReportResponse(
        Long eventId,
        String eventTitle,
        long totalTickets,
        long soldTickets,
        long checkedInTickets,
        long remainingTickets
) {
}
