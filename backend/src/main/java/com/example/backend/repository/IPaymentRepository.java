package com.example.backend.repository;

import com.example.backend.entities.Payment;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import com.example.backend.share.enums.PaymentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPaymentRepository extends IBaseEntityRepository<Payment, Long> {

    @Query("""
        select p
        from Payment p
        where p.order.id = :orderId
    """)
    Optional<Payment> findByOrderId(@Param("orderId") Long orderId);

    @Query("""
        select p
        from Payment p
        where p.order.id in :orderIds
    """)
    List<Payment> findByOrderIds(@Param("orderIds") List<Long> orderIds);

    @Query("""
        select coalesce(sum(p.amount), 0)
        from Payment p
        where p.status = :status
    """)
    BigDecimal sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("""
        select count(p)
        from Payment p
        where p.status = :status
    """)
    long countByStatus(@Param("status") PaymentStatus status);

    @Query("""
        select coalesce(sum(p.amount), 0)
        from Payment p
        where p.status = :status
          and p.paymentDate between :startDate and :endDate
    """)
    BigDecimal sumAmountByStatusAndPaymentDateBetween(@Param("status") PaymentStatus status,
                                                      @Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate);

    @Query("""
        select count(p)
        from Payment p
        where p.status = :status
          and p.paymentDate between :startDate and :endDate
    """)
    long countByStatusAndPaymentDateBetween(@Param("status") PaymentStatus status,
                                            @Param("startDate") Date startDate,
                                            @Param("endDate") Date endDate);

    @Query("""
        select new com.example.backend.dto.response.report.RevenueByEventResponse(
            e.id,
            e.title,
            coalesce(sum(oi.priceAtPurchase), 0)
        )
        from Payment p
        join p.order o
        join o.items oi
        join oi.ticket t
        join t.ticketType tt
        join tt.event e
        where p.status = :status
        group by e.id, e.title
        order by e.id
    """)
    List<com.example.backend.dto.response.report.RevenueByEventResponse> revenueByEvent(
            @Param("status") PaymentStatus status
    );

    @Query("""
        select new com.example.backend.dto.response.report.RevenueByEventResponse(
            e.id,
            e.title,
            coalesce(sum(oi.priceAtPurchase), 0)
        )
        from Payment p
        join p.order o
        join o.items oi
        join oi.ticket t
        join t.ticketType tt
        join tt.event e
        where p.status = :status
          and p.paymentDate between :startDate and :endDate
        group by e.id, e.title
        order by e.id
    """)
    List<com.example.backend.dto.response.report.RevenueByEventResponse> revenueByEventWithPaymentDateBetween(
            @Param("status") PaymentStatus status,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );
}
