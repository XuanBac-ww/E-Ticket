package com.example.backend.service.order;

import com.example.backend.dto.request.order.CreateOrderRequest;
import com.example.backend.dto.request.order.UpdateOrderStatusRequest;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.dto.response.order.OrderResponse;

public interface IOrderService {

    OrderResponse createOrder(Long userId,CreateOrderRequest request);

    PageResponse<OrderResponse> getAllOrders(Long userId, int page, int size);

    OrderResponse getOrderById(Long orderId, Long userId);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    OrderResponse cancelOrder(Long orderId, Long userId);

    void deleteOrder(Long orderId, Long userId);
}
