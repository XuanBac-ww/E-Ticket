package com.example.backend.mapper;

import com.example.backend.dto.response.order.OrderItemResponse;
import com.example.backend.entities.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IOrderItemMapper {

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "ticketId", source = "ticket.id")
    OrderItemResponse toResponse(OrderItem orderItem);
}
