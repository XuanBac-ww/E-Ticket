package com.example.backend.dto.request.ticketType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTicketTypeRequest(
        @NotBlank(message = "Ticket type name must not be blank")
        @Size(max = 100, message = "Ticket type name must not exceed 100 characters")
        String name,

        @NotNull(message = "Ticket price must not be null")
        @DecimalMin(value = "0.0", inclusive = true, message = "Ticket price must be greater than or equal to 0")
        BigDecimal price,

        @NotNull(message = "Total ticket quantity must not be null")
        @Positive(message = "Total ticket quantity must be greater than 0")
        Integer totalQuantity
) {
}
