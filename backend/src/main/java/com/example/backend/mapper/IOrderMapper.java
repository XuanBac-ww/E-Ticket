package com.example.backend.mapper;

import com.example.backend.dto.response.order.OrderResponse;
import com.example.backend.entities.Customer;
import com.example.backend.entities.Order;
import com.example.backend.entities.OrderItem;
import com.example.backend.share.enums.OrderStatus;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", uses = IOrderItemMapper.class, builder = @Builder(disableBuilder = true))
public interface IOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Order createEntity(Customer customer,
                       List<OrderItem> items,
                       BigDecimal totalAmount,
                       OrderStatus status);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "status", source = "status")
    OrderResponse toResponse(Order order);
}
