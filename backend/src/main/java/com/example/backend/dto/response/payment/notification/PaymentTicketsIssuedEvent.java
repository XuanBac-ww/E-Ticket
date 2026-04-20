package com.example.backend.dto.response.payment.notification;

public record PaymentTicketsIssuedEvent(
        Long orderId,
        String customerEmail,
        String customerName
) {
}
