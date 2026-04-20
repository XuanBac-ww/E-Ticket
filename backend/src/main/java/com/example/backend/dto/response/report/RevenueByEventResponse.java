package com.example.backend.dto.response.report;

import java.math.BigDecimal;

public record RevenueByEventResponse(
        Long eventId,
        String eventTitle,
        BigDecimal revenue
) {
}
