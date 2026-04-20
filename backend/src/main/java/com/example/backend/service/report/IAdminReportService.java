package com.example.backend.service.report;

import com.example.backend.dto.response.report.RevenueReportResponse;
import com.example.backend.dto.response.report.TicketReportResponse;

import java.time.LocalDate;

public interface IAdminReportService {
    RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate);

    TicketReportResponse getTicketReport();
}
