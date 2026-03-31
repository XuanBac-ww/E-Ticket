package com.example.backend.dto.request.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "Danh sách vé không được để trống")
        @Valid
        List< CreateOrderItemRequest> items
) {
}
