package com.example.backend.dto.request.ticket;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateTicketRequest(
        @NotEmpty(message = "Seat number list must not be empty")
        List<String> seatNumber
) {
}
