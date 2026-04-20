package com.example.backend.mapper;

import com.example.backend.dto.response.order.OrderItemResponse;
import com.example.backend.entities.Order;
import com.example.backend.entities.OrderItem;
import com.example.backend.entities.Ticket;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface IOrderItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    OrderItem createEntity(Order order, Ticket ticket, BigDecimal priceAtPurchase);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "ticketId", source = "ticket.id")
    OrderItemResponse toResponse(OrderItem orderItem);
}
