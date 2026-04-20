package com.example.backend.service.order.share;

import com.example.backend.dto.request.order.CreateOrderItemRequest;
import com.example.backend.dto.request.order.CreateOrderRequest;
import com.example.backend.share.exception.AppException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderSupportServiceTest {

    private final OrderSupportService orderSupportService = new OrderSupportService(null, null, null, null);

    @Test
    void validateCreateOrderRequestRejectsEmptyItems() {
        CreateOrderRequest request = new CreateOrderRequest(List.of());

        assertThatThrownBy(() -> orderSupportService.validateCreateOrderRequest(request))
                .isInstanceOf(AppException.class)
                .hasMessage("Ticket list must not be empty");
    }

    @Test
    void validateCreateOrderRequestRejectsDuplicateTicketIds() {
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(10L),
                new CreateOrderItemRequest(10L)
        ));

        assertThatThrownBy(() -> orderSupportService.validateCreateOrderRequest(request))
                .isInstanceOf(AppException.class)
                .hasMessage("Duplicate tickets are not allowed in the same order");
    }
}
