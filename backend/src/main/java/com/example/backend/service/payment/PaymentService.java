package com.example.backend.service.payment;

import com.example.backend.config.payment.PaymentProperties;
import com.example.backend.dto.response.payment.notification.PaymentConfirmedEvent;
import com.example.backend.dto.response.payment.notification.PaymentTicketsIssuedEvent;
import com.example.backend.dto.response.payment.PaymentResponse;
import com.example.backend.entities.Order;
import com.example.backend.entities.Payment;
import com.example.backend.mapper.IPaymentMapper;
import com.example.backend.repository.IPaymentRepository;
import com.example.backend.service.order.share.IOrderSupportService;
import com.example.backend.service.payment.support.IPaymentCodeGenerator;
import com.example.backend.share.enums.OrderStatus;
import com.example.backend.share.enums.PaymentMethod;
import com.example.backend.share.enums.PaymentStatus;
import com.example.backend.share.exception.AppException;
import com.example.backend.share.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final IPaymentRepository paymentRepository;
    private final IOrderSupportService orderSupportService;
    private final IPaymentCodeGenerator paymentCodeGenerator;
    private final PaymentProperties paymentProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final IPaymentMapper paymentMapper;

    @Override
    @Transactional(noRollbackFor = AppException.class)
    public PaymentResponse createQrPayment(Long orderId, Long userId) {
        log.info("Creating QR payment for orderId={}, userId={}", orderId, userId);
        paymentProperties.validateConfiguration();

        Order order = orderSupportService.findOrderWithItems(orderId);
        validateAccessiblePendingOrder(order, userId);

        Date now = new Date();
        Date holdExpiredAt = resolveOrderHoldExpiry(order);
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        if (!holdExpiredAt.after(now)) {
            expireOrderAndPayment(order, payment, "Order payment window has expired");
            throw new AppException("Order payment window has expired");
        }

        if (canReusePendingPayment(payment, now)) {
            log.info("Reusing pending payment for orderId={}, paymentCode={}", orderId, payment.getPaymentCode());
            return paymentMapper.toResponse(payment);
        }

        Payment managedPayment = applyQrPayment(payment, order, holdExpiredAt);

        Payment savedPayment = paymentRepository.save(managedPayment);
        log.info(
                "Created QR payment orderId={}, paymentCode={}, expiredAt={}",
                orderId,
                savedPayment.getPaymentCode(),
                savedPayment.getExpiredAt()
        );
        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional
    public PaymentResponse getCurrentPayment(Long orderId, Long userId) {
        log.debug("Fetching current payment for orderId={}, userId={}", orderId, userId);

        Order order = orderSupportService.findOrderWithItems(orderId);
        validateOrderOwner(order, userId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for this order"));

        Date now = new Date();
        if (payment.getStatus() == PaymentStatus.PENDING
                && payment.getExpiredAt() != null
                && !payment.getExpiredAt().after(now)
                && order.getStatus() == OrderStatus.PENDING) {
            expireOrderAndPayment(order, payment, "Payment window has expired");
            return paymentMapper.toResponse(payment);
        }

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(noRollbackFor = AppException.class)
    public PaymentResponse confirmPayment(Long orderId) {
        Order order = orderSupportService.findOrderWithItems(orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for this order"));

        if (order.getStatus() == OrderStatus.PAID && payment.getStatus() == PaymentStatus.SUCCESS) {
            return paymentMapper.toResponse(payment);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException("Only orders in PENDING status can be confirmed");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new AppException("Only payments in PENDING status can be confirmed");
        }

        Date now = new Date();
        if (payment.getExpiredAt() != null && !payment.getExpiredAt().after(now)) {
            expireOrderAndPayment(order, payment, "Payment confirmation arrived after expiration");
            throw new AppException("Payment window has expired");
        }

        orderSupportService.updateTicketsWhenOrderPaid(order);
        order.setStatus(OrderStatus.PAID);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(now);
        payment.setMessage(buildMessage(payment.getMessage(), "Payment confirmed manually"));

        paymentRepository.save(payment);
        publishPaymentConfirmedEvent(order, payment);
        publishTicketsIssuedEvent(order);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void cancelPendingPayment(Order order) {
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment == null || payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setMessage(buildMessage(payment.getMessage(), "Payment was cancelled with the order"));
        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void deletePaymentForOrder(Order order) {
        Payment payment = paymentRepository.findByOrderId(order.getId()).orElse(null);
        if (payment == null) {
            return;
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new AppException("Cannot delete a successful payment");
        }

        paymentRepository.delete(payment);
    }

    private void validateAccessiblePendingOrder(Order order, Long userId) {
        validateOrderOwner(order, userId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException("Payments can only be created for orders in PENDING status");
        }
        orderSupportService.validateOrderHasItems(order);
    }

    private void validateOrderOwner(Order order, Long userId) {
        if (order.getCustomer() == null || !Objects.equals(order.getCustomer().getId(), userId)) {
            throw new AppException("You do not have permission to access this order");
        }
    }

    private Date resolveOrderHoldExpiry(Order order) {
        return order.getItems().stream()
                .map(orderItem -> orderItem.getTicket().getHoldExpiresAt())
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new AppException("Order does not have a valid ticket hold expiration"));
    }

    private boolean canReusePendingPayment(Payment payment, Date now) {
        return payment != null
                && payment.getStatus() == PaymentStatus.PENDING
                && payment.getExpiredAt() != null
                && payment.getExpiredAt().after(now)
                && payment.getQrUrl() != null
                && !payment.getQrUrl().isBlank();
    }

    private Payment applyQrPayment(Payment payment, Order order, Date holdExpiredAt) {
        String paymentCode = paymentCodeGenerator.generate();
        BigDecimal amount = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
        String qrUrl = paymentProperties.resolvePaymentQrUrl();
        String message = "Scan the QR and transfer with content " + paymentCode;

        if (payment == null) {
            return paymentMapper.createEntity(
                    order,
                    amount,
                    PaymentMethod.PERSONAL_QR,
                    paymentCode,
                    qrUrl,
                    paymentProperties.getReceiverName(),
                    paymentCode,
                    holdExpiredAt,
                    PaymentStatus.PENDING,
                    message
            );
        }

        paymentMapper.updateQrPayment(
                payment,
                order,
                amount,
                PaymentMethod.PERSONAL_QR,
                paymentCode,
                qrUrl,
                paymentProperties.getReceiverName(),
                paymentCode,
                holdExpiredAt,
                PaymentStatus.PENDING,
                message
        );
        return payment;
    }

    private void expireOrderAndPayment(Order order, Payment payment, String message) {
        if (order.getStatus() == OrderStatus.PENDING) {
            orderSupportService.updateTicketsWhenOrderCancelledOrExpired(order);
            order.setStatus(OrderStatus.EXPIRED);
        }

        if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.EXPIRED);
            payment.setMessage(buildMessage(payment.getMessage(), message));
            paymentRepository.save(payment);
        }
    }

    private String buildMessage(String currentMessage, String appendMessage) {
        if (currentMessage == null || currentMessage.isBlank()) {
            return appendMessage;
        }
        return currentMessage + " | " + appendMessage;
    }

    private void publishPaymentConfirmedEvent(Order order, Payment payment) {
        if (order.getCustomer() == null) {
            return;
        }
        eventPublisher.publishEvent(new PaymentConfirmedEvent(
                order.getId(),
                order.getCustomer().getEmail(),
                order.getCustomer().getFullName(),
                payment.getAmount(),
                payment.getPaymentCode(),
                payment.getTransferContent(),
                payment.getPaymentDate()
        ));
    }

    private void publishTicketsIssuedEvent(Order order) {
        if (order.getCustomer() == null) {
            return;
        }
        eventPublisher.publishEvent(new PaymentTicketsIssuedEvent(
                order.getId(),
                order.getCustomer().getEmail(),
                order.getCustomer().getFullName()
        ));
    }

}
