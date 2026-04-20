package com.example.backend.controller.customer.order.api;

import com.example.backend.dto.request.order.CreateOrderItemRequest;
import com.example.backend.dto.response.order.OrderItemResponse;
import com.example.backend.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface OrderItemApi {

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("")
    ResponseEntity<List<OrderItemResponse>> getOrderItems(@PathVariable Long orderId,
                                                          @AuthenticationPrincipal CustomUserPrincipal principal);

    @GetMapping("/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    ResponseEntity<OrderItemResponse> getOrderItem(@PathVariable Long orderId,
                                                   @PathVariable Long itemId,
                                                   @AuthenticationPrincipal CustomUserPrincipal principal
    );

    @PostMapping("")
    @PreAuthorize("hasRole('CUSTOMER')")
    ResponseEntity<OrderItemResponse> addOrderItem(
            @PathVariable Long orderId,
            @Valid @RequestBody CreateOrderItemRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    );

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    ResponseEntity<String> deleteOrderItem(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    );
}
