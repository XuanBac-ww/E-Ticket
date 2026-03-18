package com.example.backend.controller.customer.order.api;

import com.example.backend.dto.request.order.CreateOrderRequest;
import com.example.backend.dto.request.order.UpdateOrderStatusRequest;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.dto.response.order.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface OrderApi {

    @PostMapping("")
    ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request);

    @GetMapping("")
    ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/{orderId}")
    ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId);

    @PatchMapping("/{orderId}/status")
    ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateOrderStatusRequest request
    );

    @PatchMapping("/{orderId}/cancel")
    ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId);

    @DeleteMapping("/{orderId}")
    ResponseEntity<String> deleteOrder(@PathVariable Long orderId);
}
