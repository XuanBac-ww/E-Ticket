package com.example.backend.controller.customer.order;

import com.example.backend.controller.customer.order.api.OrderItemApi;
import com.example.backend.dto.request.order.CreateOrderItemRequest;
import com.example.backend.dto.response.order.OrderItemResponse;
import com.example.backend.service.order.orderItem.IOrderItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/items")
@RequiredArgsConstructor
public class OrderItemController implements OrderItemApi {

    private final IOrderItemService orderItemService;

    @Override
    public ResponseEntity<List<OrderItemResponse>> getOrderItems(Long orderId) {
        List<OrderItemResponse> response = orderItemService.getOrderItems(orderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<OrderItemResponse> getOrderItem(Long orderId, Long itemId) {
        OrderItemResponse response = orderItemService.getOrderItem(orderId, itemId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<OrderItemResponse> addOrderItem(Long orderId,
                                                          @Valid @RequestBody CreateOrderItemRequest request) {
        OrderItemResponse response = orderItemService.addOrderItem(orderId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteOrderItem(Long orderId, Long itemId) {
        orderItemService.deleteOrderItem(orderId, itemId);
        return new ResponseEntity<>("delete successful", HttpStatus.OK);
    }
}
