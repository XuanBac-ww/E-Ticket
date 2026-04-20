package com.example.backend.dto.response.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RevenueReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalRevenue,
        long totalPayments,
        List<RevenueByEventResponse> revenueByEvents
) {
}
