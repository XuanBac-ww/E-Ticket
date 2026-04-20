package com.example.backend.dto.response.payment;

import java.math.BigDecimal;
import java.util.Date;

public record PaymentResponse(
        Long orderId,
        String paymentMethod,
        String paymentCode,
        String status,
        BigDecimal amount,
        String qrUrl,
        String receiverName,
        String transferContent,
        Date expiredAt,
        Date paymentDate,
        String message
) {
}
