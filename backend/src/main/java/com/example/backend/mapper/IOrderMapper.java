package com.example.backend.mapper;

import com.example.backend.dto.response.order.OrderResponse;
import com.example.backend.entities.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = IOrderItemMapper.class)
public interface IOrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "status", source = "status")
    OrderResponse toResponse(Order order);
}