package com.example.backend.service.report;

import com.example.backend.dto.response.report.RevenueByEventResponse;
import com.example.backend.dto.response.report.RevenueReportResponse;
import com.example.backend.dto.response.report.TicketByEventReportResponse;
import com.example.backend.dto.response.report.TicketReportResponse;
import com.example.backend.repository.IPaymentRepository;
import com.example.backend.repository.ITicketRepository;
import com.example.backend.share.enums.PaymentStatus;
import com.example.backend.share.enums.TicketStatus;
import com.example.backend.share.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService implements IAdminReportService {

    private final IPaymentRepository paymentRepository;
    private final ITicketRepository ticketRepository;

    @Override
    public RevenueReportResponse getRevenueReport(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        if (startDate != null && endDate != null) {
            Date startDateTime = toDateStart(startDate);
            Date endDateTime = toDateEnd(endDate);
            BigDecimal totalRevenue = paymentRepository.sumAmountByStatusAndPaymentDateBetween(
                    PaymentStatus.SUCCESS, startDateTime, endDateTime
            );
            long totalPayments = paymentRepository.countByStatusAndPaymentDateBetween(
                    PaymentStatus.SUCCESS, startDateTime, endDateTime
            );
            List<RevenueByEventResponse> revenueByEvents = paymentRepository
                    .revenueByEventWithPaymentDateBetween(PaymentStatus.SUCCESS, startDateTime, endDateTime);

            return new RevenueReportResponse(
                    startDate,
                    endDate,
                    totalRevenue,
                    totalPayments,
                    revenueByEvents
            );
        }

        BigDecimal totalRevenue = paymentRepository.sumAmountByStatus(PaymentStatus.SUCCESS);
        long totalPayments = paymentRepository.countByStatus(PaymentStatus.SUCCESS);
        List<RevenueByEventResponse> revenueByEvents = paymentRepository.revenueByEvent(PaymentStatus.SUCCESS);

        return new RevenueReportResponse(
                null,
                null,
                totalRevenue,
                totalPayments,
                revenueByEvents
        );
    }

    @Override
    public TicketReportResponse getTicketReport() {
        List<TicketStatus> soldStatuses = List.of(TicketStatus.SOLD, TicketStatus.USED);
        List<TicketStatus> remainingStatuses = List.of(TicketStatus.AVAILABLE, TicketStatus.HOLDING);

        List<TicketByEventReportResponse> ticketByEvents = ticketRepository
                .reportTicketByEvent(soldStatuses, remainingStatuses);

        long totalTickets = 0L;
        long soldTickets = 0L;
        long checkedInTickets = 0L;
        long remainingTickets = 0L;

        for (TicketByEventReportResponse item : ticketByEvents) {
            totalTickets += item.totalTickets();
            soldTickets += item.soldTickets();
            checkedInTickets += item.checkedInTickets();
            remainingTickets += item.remainingTickets();
        }

        return new TicketReportResponse(
                totalTickets,
                soldTickets,
                checkedInTickets,
                remainingTickets,
                ticketByEvents
        );
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return;
        }
        if (startDate == null || endDate == null) {
            throw new AppException("Both startDate and endDate must be provided");
        }
        if (startDate.isAfter(endDate)) {
            throw new AppException("startDate must be before or equal to endDate");
        }
    }

    private Date toDateStart(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date toDateEnd(LocalDate localDate) {
        return Date.from(localDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
    }
}
