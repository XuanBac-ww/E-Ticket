package com.example.backend.service.order;

import com.example.backend.dto.request.order.CreateOrderItemRequest;
import com.example.backend.dto.request.order.CreateOrderRequest;
import com.example.backend.dto.request.order.UpdateOrderStatusRequest;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.dto.response.order.OrderItemResponse;
import com.example.backend.dto.response.order.OrderResponse;

import java.util.List;

public interface IOrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    PageResponse<OrderResponse> getAllOrders(int page, int size);

    OrderResponse getOrderById(Long orderId);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    OrderResponse cancelOrder(Long orderId);

    void deleteOrder(Long orderId);
}
