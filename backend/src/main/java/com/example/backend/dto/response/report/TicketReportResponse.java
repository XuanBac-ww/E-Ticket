package com.example.backend.dto.response.report;

import java.util.List;

public record TicketReportResponse(
        long totalTickets,
        long soldTickets,
        long checkedInTickets,
        long remainingTickets,
        List<TicketByEventReportResponse> ticketByEvents
) {
}
