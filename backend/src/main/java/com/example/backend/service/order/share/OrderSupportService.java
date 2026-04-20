package com.example.backend.service.order.share;

import com.example.backend.dto.request.order.CreateOrderRequest;
import com.example.backend.dto.request.order.UpdateOrderStatusRequest;
import com.example.backend.entities.Customer;
import com.example.backend.entities.Order;
import com.example.backend.entities.OrderItem;
import com.example.backend.entities.Ticket;
import com.example.backend.entities.TicketType;
import com.example.backend.mapper.IOrderItemMapper;
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
public class OrderSupportService implements IOrderSupportService {

    private static final long HOLD_MINUTES = 15L;

    private final IOrderItemRepository orderItemRepository;
    private final ICustomerRepository customerRepository;
    private final IOrderRepository orderRepository;
    private final IOrderItemMapper orderItemMapper;

    @Override
    public Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException("Customer not found"));
    }

    @Override
    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Order not found"));
    }

    @Override
    public Order findOrderWithItems(Long orderId) {
        return orderRepository.findByIdWithItemsAndTickets(orderId)
                .orElseThrow(() -> new AppException("Order not found"));
    }

    @Override
    public OrderItem findOrderItemByOrderId(Long itemId, Long orderId) {
        return orderItemRepository.findByIdAndOrderId(itemId, orderId)
                .orElseThrow(() -> new AppException("Order item not found"));
    }

    @Override
    public void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new AppException("Ticket list must not be empty");
        }

        boolean hasInvalidTicket = request.items().stream()
                .anyMatch(item -> item == null || item.ticketId() == null);
        if (hasInvalidTicket) {
            throw new AppException("Ticket id must not be null");
        }

        long distinctTicketCount = request.items().stream()
                .map(item -> item.ticketId())
                .distinct()
                .count();
        if (distinctTicketCount != request.items().size()) {
            throw new AppException("Duplicate tickets are not allowed in the same order");
        }
    }

    @Override
    public void validateOrderHasItems(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new AppException("Order has no items to update status");
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
            throw new AppException("Ticket is not available for purchase: " + ticket.getId());
        }

        TicketType ticketType = ticket.getTicketType();
        if (ticketType == null) {
            throw new AppException("Ticket type not found for ticket: " + ticket.getId());
        }

        if (ticketType.getRemainingQuantity() == null || ticketType.getRemainingQuantity() <= 0) {
            throw new AppException("Ticket type is sold out");
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
            throw new AppException("Ticket type not found for ticket: " + ticket.getId());
        }

        if (ticketType.getRemainingQuantity() == null) {
            throw new AppException("Invalid remaining quantity for ticket type: " + ticketType.getId());
        }

        ticket.setStatus(TicketStatus.AVAILABLE);
        ticket.setHoldExpiresAt(null);
        ticketType.setRemainingQuantity(ticketType.getRemainingQuantity() + 1);
    }

    @Override
    public OrderItem buildOrderItem(Order order, Ticket ticket, BigDecimal price) {
        return orderItemMapper.createEntity(order, ticket, price);
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
                throw new AppException("Ticket is not in HOLDING status: " + ticket.getId());
            }

            if (ticket.getHoldExpiresAt() == null || ticket.getHoldExpiresAt().before(now)) {
                throw new AppException("Ticket hold has expired: " + ticket.getId());
            }

            ticket.setStatus(TicketStatus.SOLD);
            ticket.setHoldExpiresAt(null);
        }
    }

    @Override
    public void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new AppException("New status must be different from the current status");
        }

        if (currentStatus == OrderStatus.PAID) {
            throw new AppException("Cannot update status from PAID");
        }

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new AppException("Cannot update status from CANCELLED");
        }

        if (currentStatus == OrderStatus.EXPIRED) {
            throw new AppException("Cannot update status from EXPIRED");
        }

        if (currentStatus == OrderStatus.PENDING
                && newStatus != OrderStatus.PAID
                && newStatus != OrderStatus.CANCELLED
                && newStatus != OrderStatus.EXPIRED) {
            throw new AppException("Only transitions from PENDING to PAID, CANCELLED, or EXPIRED are allowed");
        }
    }

    @Override
    public OrderStatus parseOrderStatus(UpdateOrderStatusRequest request) {
        try {
            return OrderStatus.valueOf(request.status().trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new AppException("Invalid order status");
        }
    }
}
