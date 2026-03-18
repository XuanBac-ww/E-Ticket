package com.example.backend.service.order.share;

import com.example.backend.dto.request.order.CreateOrderRequest;
import com.example.backend.dto.request.order.UpdateOrderStatusRequest;
import com.example.backend.entities.*;
import com.example.backend.share.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.Date;

public interface IOrderSupportService {
    Customer findCustomerById(Long customerId);

    Order findOrderById(Long orderId);

    Order findOrderWithItems(Long orderId);

    OrderItem findOrderItemByOrderId(Long itemId, Long orderId);

    void validateCreateOrderRequest(CreateOrderRequest request);

    void validateOrderHasItems(Order order);

    Date buildHoldExpiresAt();

    boolean isHoldExpired(Ticket ticket, Date now);

    TicketType validatePurchasableTicket(Ticket ticket, Date now);

    void holdTicket(Ticket ticket, TicketType ticketType, Date holdExpiresAt);

    void releaseTicketIfHolding(Ticket ticket);

    OrderItem buildOrderItem(Order order, Ticket ticket, BigDecimal price);

    void updateTicketsWhenOrderCancelledOrExpired(Order order);

    void updateTicketsWhenOrderPaid(Order order);

    void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus);

    OrderStatus parseOrderStatus(UpdateOrderStatusRequest request);
}
