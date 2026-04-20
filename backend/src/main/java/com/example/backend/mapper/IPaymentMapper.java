package com.example.backend.mapper;

import com.example.backend.dto.response.payment.PaymentResponse;
import com.example.backend.entities.Order;
import com.example.backend.entities.Payment;
import com.example.backend.share.enums.PaymentMethod;
import com.example.backend.share.enums.PaymentStatus;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.Date;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface IPaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "order", source = "order")
    @Mapping(target = "status", source = "paymentStatus")
    Payment createEntity(Order order,
                         BigDecimal amount,
                         PaymentMethod paymentMethod,
                         String paymentCode,
                         String qrUrl,
                         String receiverName,
                         String transferContent,
                         Date expiredAt,
                         PaymentStatus paymentStatus,
                         String message);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "order", source = "order")
    @Mapping(target = "status", source = "paymentStatus")
    void updateQrPayment(@MappingTarget Payment payment,
                         Order order,
                         BigDecimal amount,
                         PaymentMethod paymentMethod,
                         String paymentCode,
                         String qrUrl,
                         String receiverName,
                         String transferContent,
                         Date expiredAt,
                         PaymentStatus paymentStatus,
                         String message);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "status", source = "status")
    PaymentResponse toResponse(Payment payment);
}
