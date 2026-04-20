package com.example.backend.dto.request.ticket;

import jakarta.validation.constraints.NotBlank;

public record TicketCheckInRequest(
        @NotBlank(message = "qrCodeHash must not be blank")
        String qrCodeHash
) {
}
