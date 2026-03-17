package com.example.backend.dto.request.ticketType;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateTicketTypeRequest(
        @NotBlank(message = "Tên loại vé không được để trống")
        @Size(max = 100, message = "Tên loại vé không được vượt quá 100 ký tự")
        String name,

        @NotNull(message = "Giá vé không được để trống")
        @DecimalMin(value = "0.0", inclusive = true, message = "Giá vé phải lớn hơn hoặc bằng 0")
        BigDecimal price,

        @NotNull(message = "Tổng số lượng vé không được để trống")
        @Positive(message = "Tổng số lượng vé phải lớn hơn 0")
        Integer totalQuantity
) {
}
