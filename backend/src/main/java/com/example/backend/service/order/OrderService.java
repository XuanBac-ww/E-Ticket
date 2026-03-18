package com.example.backend.service.order;

import com.example.backend.dto.request.order.CreateOrderItemRequest;
import com.example.backend.dto.request.order.CreateOrderRequest;
import com.example.backend.dto.request.order.UpdateOrderStatusRequest;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.dto.response.order.OrderResponse;
import com.example.backend.entities.Customer;
import com.example.backend.entities.Order;
import com.example.backend.entities.OrderItem;
import com.example.backend.entities.Ticket;
import com.example.backend.entities.TicketType;
import com.example.backend.mapper.IOrderMapper;
import com.example.backend.repository.IOrderRepository;
import com.example.backend.repository.ITicketRepository;
import com.example.backend.service.order.share.IOrderSupportService;
import com.example.backend.share.enums.OrderStatus;
import com.example.backend.share.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final IOrderSupportService orderSupportService;
    private final IOrderRepository orderRepository;
    private final IOrderMapper orderMapper;
    private final ITicketRepository ticketRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = orderSupportService.findCustomerById(request.customerId());
        orderSupportService.validateCreateOrderRequest(request);

        List<Long> ticketIds = request.items().stream()
                .map(CreateOrderItemRequest::ticketId)
                .toList();

        List<Ticket> tickets = ticketRepository.findAllByIdInFetchTicketType(ticketIds);

        Map<Long, Ticket> ticketMap = tickets.stream()
                .collect(Collectors.toMap(Ticket::getId, Function.identity()));

        List<OrderItem> orderItems = new ArrayList<>(request.items().size());

        Order order = Order.builder()
                .customer(customer)
                .status(OrderStatus.PENDING)
                .items(orderItems)
                .totalAmount(BigDecimal.ZERO)
                .build();

        Date now = new Date();
        Date holdExpiresAt = orderSupportService.buildHoldExpiresAt();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest : request.items()) {
            Ticket ticket = ticketMap.get(itemRequest.ticketId());
            if (ticket == null) {
                throw new AppException("Ticket not found: " + itemRequest.ticketId());
            }

            TicketType ticketType = orderSupportService.validatePurchasableTicket(ticket, now);

            orderSupportService.holdTicket(ticket, ticketType, holdExpiresAt);

            BigDecimal price = ticketType.getPrice();
            OrderItem orderItem = orderSupportService.buildOrderItem(order, ticket, price);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(price);
        }

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public PageResponse<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAll(pageable);

        List<OrderResponse> orderList = orders.getContent().stream()
                .map(orderMapper::toResponse)
                .toList();

        return new PageResponse<>(
                200,
                true,
                "Lấy tất cả các order thành công",
                orderList,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isLast()
        );
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderSupportService.findOrderById(orderId);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderSupportService.findOrderWithItems(orderId);

        orderSupportService.validateOrderHasItems(order);

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = orderSupportService.parseOrderStatus(request);

        orderSupportService.validateStatusTransition(currentStatus, newStatus);

        switch (newStatus) {
            case PAID -> orderSupportService.updateTicketsWhenOrderPaid(order);
            case CANCELLED, EXPIRED -> orderSupportService.updateTicketsWhenOrderCancelledOrExpired(order);
            default -> {
            }
        }

        order.setStatus(newStatus);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderSupportService.findOrderWithItems(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException("Chỉ được hủy đơn hàng khi ở trạng thái PENDING");
        }

        orderSupportService.updateTicketsWhenOrderCancelledOrExpired(order);
        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderSupportService.findOrderWithItems(orderId);

        if (order.getStatus() == OrderStatus.PAID) {
            throw new AppException("Không được xóa, đơn hàng đã được thanh toán");
        }

        if (order.getStatus() == OrderStatus.PENDING) {
            orderSupportService.updateTicketsWhenOrderCancelledOrExpired(order);
        }

        orderRepository.delete(order);
    }



}