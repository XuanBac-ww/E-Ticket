package com.example.backend.mapper;

import com.example.backend.dto.response.order.OrderResponse;
import com.example.backend.entities.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IOrderMapper {
    OrderResponse toResponse(Order order);
}
