package com.example.backend.schedules;

import com.example.backend.service.payment.expiration.IPendingOrderExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PendingOrderExpirationScheduler {

    private final IPendingOrderExpirationService pendingOrderExpirationService;

    @Scheduled(
            fixedDelayString = "${app.payment.expiration-check-delay-ms:60000}",
            initialDelayString = "${app.payment.expiration-check-initial-delay-ms:15000}"
    )
    public void expireOverdueOrders() {
        try {
            pendingOrderExpirationService.expireOverduePendingOrders();
        } catch (Exception ex) {
            log.error("Failed to auto-expire overdue orders", ex);
        }
    }
}
