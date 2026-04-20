package com.example.backend.service.payment;

import com.example.backend.dto.response.payment.PaymentResponse;
import com.example.backend.entities.Order;

public interface IPaymentService {

    PaymentResponse createQrPayment(Long orderId, Long userId);

    PaymentResponse getCurrentPayment(Long orderId, Long userId);

    PaymentResponse confirmPayment(Long orderId);

    void cancelPendingPayment(Order order);

    void deletePaymentForOrder(Order order);
}
