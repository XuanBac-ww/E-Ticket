package com.example.backend.dto.response.Ticket;

import com.example.backend.share.enums.TicketStatus;

import java.util.Date;

public record MyTicketResponse(
        Long id,
        Long ticketTypeId,
        String seatNumber,
        TicketStatus status,
        boolean checkedIn,
        Date checkedInAt,
        String qrUrl
) {
}
