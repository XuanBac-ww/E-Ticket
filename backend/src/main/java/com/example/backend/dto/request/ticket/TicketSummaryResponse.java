package com.example.backend.dto.request.ticket;

import lombok.Builder;

@Builder
public class TicketSummaryResponse {
    private String ticketTypeName;
    private Integer totalLimit;
    private Integer remainingInField;
    private Long actualAvailable;
    private Long totalCreated;
    private Long totalSold;
}
