package com.example.backend.service.order.orderItem;

import com.example.backend.dto.request.order.CreateOrderItemRequest;
import com.example.backend.dto.response.order.OrderItemResponse;

import java.util.List;

public interface IOrderItemService {

    List<OrderItemResponse> getOrderItems(Long orderId);

    OrderItemResponse getOrderItem(Long orderId, Long itemId);

    OrderItemResponse addOrderItem(Long orderId, CreateOrderItemRequest request);

    void deleteOrderItem(Long orderId, Long itemId);
}
