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
import com.example.backend.service.payment.IPaymentService;
import com.example.backend.share.enums.OrderStatus;
import com.example.backend.share.exception.AppException;
import com.example.backend.share.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final IOrderSupportService orderSupportService;
    private final IOrderRepository orderRepository;
    private final IOrderMapper orderMapper;
    private final ITicketRepository ticketRepository;
    private final IPaymentService paymentService;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        log.info("Creating order for userId={} with itemCount={}", userId, request.items().size());
        Customer customer = orderSupportService.findCustomerById(userId);
        orderSupportService.validateCreateOrderRequest(request);

        List<Long> ticketIds = request.items().stream()
                .map(CreateOrderItemRequest::ticketId)
                .toList();

        List<Ticket> tickets = ticketRepository.findAllByIdInFetchTicketType(ticketIds);

        Map<Long, Ticket> ticketMap = tickets.stream()
                .collect(Collectors.toMap(Ticket::getId, Function.identity()));

        List<OrderItem> orderItems = new ArrayList<>(request.items().size());

        Order order = orderMapper.createEntity(customer, orderItems, BigDecimal.ZERO, OrderStatus.PENDING);

        Date now = new Date();
        Date holdExpiresAt = orderSupportService.buildHoldExpiresAt();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest : request.items()) {
            Ticket ticket = ticketMap.get(itemRequest.ticketId());
            if (ticket == null) {
                throw new ResourceNotFoundException("Ticket not found: " + itemRequest.ticketId());
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
        log.info(
                "Created orderId={} for userId={} with itemCount={} totalAmount={}",
                savedOrder.getId(),
                userId,
                savedOrder.getItems().size(),
                savedOrder.getTotalAmount()
        );
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public PageResponse<OrderResponse> getAllOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByCustomerId(userId, pageable);

        List<OrderResponse> orderList = orders.getContent().stream()
                .map(orderMapper::toResponse)
                .toList();
        log.debug("Fetched orders userId={} page={} size={} resultCount={}", userId, page, size, orderList.size());

        return new PageResponse<>(
                200,
                true,
                "Orders retrieved successfully",
                orderList,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isLast()
        );
    }

    @Override
    public OrderResponse getOrderById(Long orderId, Long userId) {
        log.debug("Fetching order detail orderId={} userId={}", orderId, userId);
        Order order = findCustomerOrderWithItems(orderId, userId);
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderSupportService.findOrderWithItems(orderId);

        orderSupportService.validateOrderHasItems(order);

        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = orderSupportService.parseOrderStatus(request);
        log.info("Updating order status orderId={} from {} to {}", orderId, currentStatus, newStatus);

        orderSupportService.validateStatusTransition(currentStatus, newStatus);

        switch (newStatus) {
            case PAID -> orderSupportService.updateTicketsWhenOrderPaid(order);
            case CANCELLED, EXPIRED -> orderSupportService.updateTicketsWhenOrderCancelledOrExpired(order);
            default -> {
            }
        }

        order.setStatus(newStatus);

        Order savedOrder = orderRepository.save(order);
        log.info("Updated order status orderId={} to {}", orderId, savedOrder.getStatus());
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = findCustomerOrderWithItems(orderId, userId);
        log.info("Cancelling orderId={} userId={} currentStatus={}", orderId, userId, order.getStatus());

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cannot cancel orderId={} because currentStatus={}", orderId, order.getStatus());
            throw new AppException("Only orders in PENDING status can be cancelled");
        }

        paymentService.cancelPendingPayment(order);
        orderSupportService.updateTicketsWhenOrderCancelledOrExpired(order);
        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);
        log.info("Cancelled orderId={}", orderId);
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId, Long userId) {
        Order order = findCustomerOrderWithItems(orderId, userId);
        log.info("Deleting orderId={} userId={} currentStatus={}", orderId, userId, order.getStatus());

        if (order.getStatus() == OrderStatus.PAID) {
            log.warn("Refusing to delete paid orderId={}", orderId);
            throw new AppException("Paid orders cannot be deleted");
        }

        if (order.getStatus() == OrderStatus.PENDING) {
            orderSupportService.updateTicketsWhenOrderCancelledOrExpired(order);
        }

        paymentService.deletePaymentForOrder(order);
        orderRepository.delete(order);
        log.info("Deleted orderId={}", orderId);
    }

    private Order findCustomerOrderWithItems(Long orderId, Long userId) {
        return orderRepository.findByIdAndCustomerIdWithItemsAndTickets(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}
