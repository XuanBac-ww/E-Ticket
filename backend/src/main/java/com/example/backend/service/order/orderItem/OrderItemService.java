package com.example.backend.service.order.orderItem;

import com.example.backend.dto.request.order.CreateOrderItemRequest;
import com.example.backend.dto.response.order.OrderItemResponse;
import com.example.backend.entities.Order;
import com.example.backend.entities.OrderItem;
import com.example.backend.entities.Ticket;
import com.example.backend.entities.TicketType;
import com.example.backend.mapper.IOrderItemMapper;
import com.example.backend.repository.IOrderItemRepository;
import com.example.backend.repository.IOrderRepository;
import com.example.backend.repository.ITicketRepository;
import com.example.backend.service.order.share.IOrderSupportService;
import com.example.backend.share.enums.OrderStatus;
import com.example.backend.share.exception.AppException;
import com.example.backend.share.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class OrderItemService implements IOrderItemService {

    private final IOrderItemMapper orderItemMapper;
    private final IOrderItemRepository orderItemRepository;
    private final IOrderRepository orderRepository;
    private final IOrderSupportService orderSupportService;
    private final ITicketRepository ticketRepository;

    @Override
    public List<OrderItemResponse> getOrderItems(Long orderId, Long userId) {
        Order order = findCustomerOrderWithItems(orderId, userId);
        return order.getItems().stream()
                .map(orderItemMapper::toResponse)
                .toList();
    }

    @Override
    public OrderItemResponse getOrderItem(Long orderId, Long itemId, Long userId) {
        validateOrderOwner(orderSupportService.findOrderById(orderId), userId);

        OrderItem orderItem = orderItemRepository.findByIdAndOrderId(itemId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        return orderItemMapper.toResponse(orderItem);
    }

    @Override
    @Transactional
    public OrderItemResponse addOrderItem(Long orderId, CreateOrderItemRequest request, Long userId) {
        Order order = findCustomerOrderWithItems(orderId, userId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException("Items can only be added when the order is in PENDING status");
        }

        boolean existedTicketInOrder = order.getItems().stream()
                .anyMatch(item -> item.getTicket().getId().equals(request.ticketId()));

        if (existedTicketInOrder) {
            throw new AppException("Ticket already exists in the order");
        }

        Date now = new Date();
        Date holdExpiresAt = orderSupportService.buildHoldExpiresAt();

        Ticket ticket = ticketRepository.findByIdInFetchTicketType(request.ticketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        TicketType ticketType = orderSupportService.validatePurchasableTicket(ticket, now);

        orderSupportService.holdTicket(ticket, ticketType, holdExpiresAt);

        OrderItem orderItem = orderSupportService.buildOrderItem(order, ticket, ticketType.getPrice());

        order.getItems().add(orderItem);
        order.setTotalAmount(order.getTotalAmount().add(ticketType.getPrice()));

        Order savedOrder = orderRepository.save(order);

        OrderItem savedItem = savedOrder.getItems().stream()
                .filter(item -> item.getTicket().getId().equals(ticket.getId()))
                .findFirst()
                .orElse(orderItem);

        return orderItemMapper.toResponse(savedItem);
    }

    @Override
    @Transactional
    public void deleteOrderItem(Long orderId, Long itemId, Long userId) {
        Order order = findCustomerOrderWithItems(orderId, userId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException("Items can only be deleted when the order is in PENDING status");
        }

        OrderItem item = orderSupportService.findOrderItemByOrderId(itemId, orderId);

        Ticket ticket = item.getTicket();
        orderSupportService.releaseTicketIfHolding(ticket);

        order.getItems().removeIf(orderItem -> orderItem.getId().equals(item.getId()));
        order.setTotalAmount(order.getTotalAmount().subtract(item.getPriceAtPurchase()));

        orderItemRepository.delete(item);

        if (order.getItems().isEmpty()) {
            order.setStatus(OrderStatus.CANCELLED);
        }

        orderRepository.save(order);
    }

    private Order findCustomerOrderWithItems(Long orderId, Long userId) {
        Order order = orderSupportService.findOrderWithItems(orderId);
        validateOrderOwner(order, userId);
        return order;
    }

    private void validateOrderOwner(Order order, Long userId) {
        if (order.getCustomer() == null || !Objects.equals(order.getCustomer().getId(), userId)) {
            throw new ResourceNotFoundException("Order not found");
        }
    }
}
