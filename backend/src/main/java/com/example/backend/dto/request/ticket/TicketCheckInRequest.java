package com.example.backend.dto.request.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCheckInRequest {
    @NotNull(message = "qr không được trống ")
    private String qrCodeHash;
}
