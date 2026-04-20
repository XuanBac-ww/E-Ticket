package com.example.backend.service.payment.expiration;

import com.example.backend.entities.Order;
import com.example.backend.entities.Payment;
import com.example.backend.repository.IOrderRepository;
import com.example.backend.repository.IPaymentRepository;
import com.example.backend.service.order.share.IOrderSupportService;
import com.example.backend.share.enums.OrderStatus;
import com.example.backend.share.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PendingOrderExpirationService implements IPendingOrderExpirationService{

    private final IOrderRepository orderRepository;
    private final IPaymentRepository paymentRepository;
    private final IOrderSupportService orderSupportService;

    @Transactional
    @Override
    public void expireOverduePendingOrders() {
        Date now = new Date();
        List<Order> overdueOrders = orderRepository.findPendingOrdersWithExpiredHolds(OrderStatus.PENDING, now);

        if (overdueOrders.isEmpty()) {
            return;
        }

        List<Long> orderIds = overdueOrders.stream()
                .map(Order::getId)
                .toList();

        Map<Long, Payment> paymentsByOrderId = paymentRepository.findByOrderIds(orderIds).stream()
                .collect(Collectors.toMap(payment -> payment.getOrder().getId(), Function.identity()));

        int expiredCount = 0;
        for (Order order : overdueOrders) {
            Payment payment = paymentsByOrderId.get(order.getId());

            if (payment != null && payment.getStatus() == PaymentStatus.SUCCESS) {
                log.warn(
                        "Skipping auto-expiration for orderId={} because paymentId={} is already SUCCESS",
                        order.getId(),
                        payment.getId()
                );
                continue;
            }

            orderSupportService.updateTicketsWhenOrderCancelledOrExpired(order);
            order.setStatus(OrderStatus.EXPIRED);

            if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.EXPIRED);
                payment.setMessage(appendMessage(payment.getMessage()));
            }

            expiredCount++;
        }

        if (expiredCount > 0) {
            log.info("Auto-expired {} pending order(s)", expiredCount);
        }

    }

    private String appendMessage(String currentMessage) {
        if (currentMessage == null || currentMessage.isBlank()) {
            return "Payment expired automatically after 15 minutes";
        }
        return currentMessage + " | " + "Payment expired automatically after 15 minutes";
    }
}
