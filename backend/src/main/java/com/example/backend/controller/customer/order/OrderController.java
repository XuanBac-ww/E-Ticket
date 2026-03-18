package com.example.backend.controller.customer.order;

import com.example.backend.controller.customer.order.api.OrderApi;
import com.example.backend.dto.request.order.CreateOrderRequest;
import com.example.backend.dto.request.order.UpdateOrderStatusRequest;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.dto.response.order.OrderResponse;
import com.example.backend.service.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final IOrderService orderService;

    @Override
    public ResponseEntity<OrderResponse> createOrder(CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(int page, int size) {
        PageResponse<OrderResponse> response = orderService.getAllOrders(page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<OrderResponse> getOrderById(Long orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<OrderResponse> updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<OrderResponse> cancelOrder(Long orderId) {
        OrderResponse response = orderService.cancelOrder(orderId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteOrder(Long orderId) {
        orderService.deleteOrder(orderId);
        return new ResponseEntity<>("delete successful", HttpStatus.OK);
    }
}
