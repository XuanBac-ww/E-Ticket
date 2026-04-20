package com.example.backend.dto.response.Ticket;

public record TicketCheckInValidationResponse(
        boolean valid,
        String message,
        Long ticketId,
        Long eventId,
        String eventTitle,
        String seatNumber
) {
}
