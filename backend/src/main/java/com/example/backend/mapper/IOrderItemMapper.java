package com.example.backend.mapper;

import com.example.backend.dto.response.order.OrderItemResponse;
import com.example.backend.entities.OrderItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IOrderItemMapper {
    OrderItemResponse toResponse(OrderItem orderItem);
}
