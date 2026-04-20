package com.example.backend.dto.response.Ticket;

import java.util.Date;

public record TicketCheckInResultResponse(
        Long ticketId,
        Long eventId,
        String eventTitle,
        String seatNumber,
        String status,
        boolean checkedIn,
        Date checkedInAt
) {
}
