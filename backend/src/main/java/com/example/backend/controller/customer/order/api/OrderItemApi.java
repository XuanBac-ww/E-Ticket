package com.example.backend.controller.customer.order.api;

import com.example.backend.dto.request.order.CreateOrderItemRequest;
import com.example.backend.dto.response.order.OrderItemResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface OrderItemApi {

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("")
    ResponseEntity<List<OrderItemResponse>> getOrderItems(@PathVariable Long orderId);

    @GetMapping("/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    ResponseEntity<OrderItemResponse> getOrderItem(@PathVariable Long orderId,
                                                   @PathVariable Long itemId
    );

    @PostMapping("")
    @PreAuthorize("hasRole('CUSTOMER')")
    ResponseEntity<OrderItemResponse> addOrderItem(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateOrderItemRequest request
    );

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    ResponseEntity<String> deleteOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId
    );
}
