package com.example.backend.dto.response.payment.notification;

import java.math.BigDecimal;
import java.util.Date;

public record PaymentConfirmedEvent(
        Long orderId,
        String customerEmail,
        String customerName,
        BigDecimal amount,
        String paymentCode,
        String transferContent,
        Date paymentDate
) {
}
