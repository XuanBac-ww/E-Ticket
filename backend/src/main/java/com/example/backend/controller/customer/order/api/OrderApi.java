package com.example.backend.controller.customer.order.api;

import com.example.backend.dto.request.order.CreateOrderRequest;
import com.example.backend.dto.request.order.UpdateOrderStatusRequest;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.dto.response.order.OrderResponse;
import com.example.backend.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

public interface OrderApi {

    @PostMapping("")
    @PreAuthorize("hasRole('CUSTOMER')")
    ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
                                              @AuthenticationPrincipal CustomUserPrincipal principal);

    @GetMapping("")
    @PreAuthorize("hasRole('CUSTOMER')")
    ResponseEntity<PageResponse<OrderResponse>> getAllOrders(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size
    );

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId);

    @PatchMapping("/{orderId}/status")
    ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request
    );

    @PreAuthorize("hasRole('CUSTOMER')")
    @PatchMapping("/{orderId}/cancel")
    ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId);

    @DeleteMapping("/{orderId}")
    ResponseEntity<String> deleteOrder(@PathVariable Long orderId);
}
