package com.example.backend.service.order.share;

import com.example.backend.dto.request.order.CreateOrderRequest;
import com.example.backend.dto.request.order.UpdateOrderStatusRequest;
import com.example.backend.entities.*;
import com.example.backend.repository.ICustomerRepository;
import com.example.backend.repository.IOrderItemRepository;
import com.example.backend.repository.IOrderRepository;
import com.example.backend.share.enums.OrderStatus;
import com.example.backend.share.enums.TicketStatus;
import com.example.backend.share.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;


@Service
@RequiredArgsConstructor
public class OrderSupportService implements IOrderSupportService{

    private static final long HOLD_MINUTES = 15L;

    private final IOrderItemRepository orderItemRepository;
    private final ICustomerRepository customerRepository;
    private final IOrderRepository orderRepository;

    @Override
    public Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException("Customer not Found"));
    }

    @Override
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Order Not Found"));
    }

    @Override
    public Order findOrderWithItems(Long orderId) {
        return orderRepository.findByIdWithItemsAndTickets(orderId)
                .orElseThrow(() -> new AppException("Order Not Found"));
    }

    @Override
    public OrderItem findOrderItemByOrderId(Long itemId, Long orderId) {
        return orderItemRepository.findByIdAndOrderId(itemId, orderId)
                .orElseThrow(() -> new AppException("Order item không tồn tại"));
    }

    @Override
    public void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new AppException("Danh sách vé không được để trống");
        }
    }

    @Override
    public void validateOrderHasItems(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new AppException("Order không có item để cập nhật trạng thái");
        }
    }

    @Override
    public Date buildHoldExpiresAt() {
        return Date.from(Instant.now().plus(HOLD_MINUTES, ChronoUnit.MINUTES));
    }

    @Override
    public boolean isHoldExpired(Ticket ticket, Date now) {
        return ticket.getHoldExpiresAt() != null && ticket.getHoldExpiresAt().before(now);
    }

    @Override
    public TicketType validatePurchasableTicket(Ticket ticket, Date now) {
        boolean holdExpired = isHoldExpired(ticket, now);

        boolean canBuy = ticket.getStatus() == TicketStatus.AVAILABLE
                || (ticket.getStatus() == TicketStatus.HOLDING && holdExpired);

        if (!canBuy) {
            throw new AppException("Ticket không khả dụng để mua: " + ticket.getId());
        }

        TicketType ticketType = ticket.getTicketType();
        if (ticketType == null) {
            throw new AppException("Ticket type not found for ticket: " + ticket.getId());
        }

        if (ticketType.getRemainingQuantity() == null || ticketType.getRemainingQuantity() <= 0) {
            throw new AppException("Loại vé đã hết số lượng");
        }

        return ticketType;
    }

    @Override
    public void holdTicket(Ticket ticket, TicketType ticketType, Date holdExpiresAt) {
        ticket.setStatus(TicketStatus.HOLDING);
        ticket.setHoldExpiresAt(holdExpiresAt);
        ticketType.setRemainingQuantity(ticketType.getRemainingQuantity() - 1);
    }

    @Override
    public void releaseTicketIfHolding(Ticket ticket) {
        if (ticket == null || ticket.getStatus() != TicketStatus.HOLDING) {
            return;
        }

        TicketType ticketType = ticket.getTicketType();
        if (ticketType == null) {
            throw new AppException("Ticket type không tồn tại cho ticket: " + ticket.getId());
        }

        if (ticketType.getRemainingQuantity() == null) {
            throw new AppException("Remaining quantity không hợp lệ cho ticket type: " + ticketType.getId());
        }

        ticket.setStatus(TicketStatus.AVAILABLE);
        ticket.setHoldExpiresAt(null);
        ticketType.setRemainingQuantity(ticketType.getRemainingQuantity() + 1);
    }

    @Override
    public OrderItem buildOrderItem(Order order, Ticket ticket, BigDecimal price) {
        return OrderItem.builder()
                .order(order)
                .ticket(ticket)
                .priceAtPurchase(price)
                .build();
    }

    @Override
    public void updateTicketsWhenOrderCancelledOrExpired(Order order) {
        for (OrderItem item : order.getItems()) {
            releaseTicketIfHolding(item.getTicket());
        }
    }

    @Override
    public void updateTicketsWhenOrderPaid(Order order) {
        Date now = new Date();

        for (OrderItem item : order.getItems()) {
            Ticket ticket = item.getTicket();

            if (ticket.getStatus() != TicketStatus.HOLDING) {
                throw new AppException("Ticket không ở trạng thái HOLD: " + ticket.getId());
            }

            if (ticket.getHoldExpiresAt() == null || ticket.getHoldExpiresAt().before(now)) {
                throw new AppException("Ticket đã hết thời gian giữ chỗ: " + ticket.getId());
            }

            ticket.setStatus(TicketStatus.SOLD);
            ticket.setHoldExpiresAt(null);
        }
    }

    @Override
    public void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new AppException("Trạng thái mới phải khác trạng thái hiện tại");
        }

        if (currentStatus == OrderStatus.PAID) {
            throw new AppException("Không thể cập nhật trạng thái từ PAID");
        }

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new AppException("Không thể cập nhật trạng thái từ CANCELLED");
        }

        if (currentStatus == OrderStatus.EXPIRED) {
            throw new AppException("Không thể cập nhật trạng thái từ EXPIRED");
        }

        if (currentStatus == OrderStatus.PENDING
                && newStatus != OrderStatus.PAID
                && newStatus != OrderStatus.CANCELLED
                && newStatus != OrderStatus.EXPIRED) {
            throw new AppException("Chỉ được chuyển từ PENDING sang PAID, CANCELLED hoặc EXPIRED");
        }
    }

    @Override
    public OrderStatus parseOrderStatus(UpdateOrderStatusRequest request) {
        try {
            return OrderStatus.valueOf(request.status().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AppException("Trạng thái đơn hàng không hợp lệ");
        }
    }
}
