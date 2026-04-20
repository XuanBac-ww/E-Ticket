package com.example.backend.service.order.orderItem;

import com.example.backend.dto.request.order.CreateOrderItemRequest;
import com.example.backend.dto.response.order.OrderItemResponse;

import java.util.List;

public interface IOrderItemService {

    List<OrderItemResponse> getOrderItems(Long orderId, Long userId);

    OrderItemResponse getOrderItem(Long orderId, Long itemId, Long userId);

    OrderItemResponse addOrderItem(Long orderId, CreateOrderItemRequest request, Long userId);

    void deleteOrderItem(Long orderId, Long itemId, Long userId);
}
