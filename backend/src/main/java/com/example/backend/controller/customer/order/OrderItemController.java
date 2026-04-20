package com.example.backend.controller.customer.order;

import com.example.backend.controller.customer.order.api.OrderItemApi;
import com.example.backend.dto.request.order.CreateOrderItemRequest;
import com.example.backend.dto.response.order.OrderItemResponse;
import com.example.backend.security.CustomUserPrincipal;
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
    public ResponseEntity<List<OrderItemResponse>> getOrderItems(Long orderId, CustomUserPrincipal principal) {
        List<OrderItemResponse> response = orderItemService.getOrderItems(orderId, principal.getUserId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<OrderItemResponse> getOrderItem(Long orderId, Long itemId,
                                                          CustomUserPrincipal principal) {
        OrderItemResponse response = orderItemService.getOrderItem(orderId, itemId, principal.getUserId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<OrderItemResponse> addOrderItem(Long orderId,
                                                          @Valid @RequestBody CreateOrderItemRequest request,
                                                          CustomUserPrincipal principal) {
        OrderItemResponse response = orderItemService.addOrderItem(orderId, request, principal.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<String> deleteOrderItem(Long orderId, Long itemId,
                                                  CustomUserPrincipal principal) {
        orderItemService.deleteOrderItem(orderId, itemId, principal.getUserId());
        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }
}
