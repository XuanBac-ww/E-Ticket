package com.example.backend.repository;

import com.example.backend.entities.Order;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import com.example.backend.share.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface IOrderRepository extends IBaseEntityRepository<Order,Long> {

    @EntityGraph(attributePaths = {"items", "items.ticket", "items.ticket.ticketType"})
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    @Query("""
        select distinct o
        from Order o
        left join fetch o.items i
        left join fetch i.ticket t
        left join fetch t.ticketType tt
        where o.id = :orderId
    """)
    Optional<Order> findByIdWithItemsAndTickets(Long orderId);

    @Query("""
        select distinct o
        from Order o
        left join fetch o.items i
        left join fetch i.ticket t
        left join fetch t.ticketType tt
        where o.id = :orderId
          and o.customer.id = :customerId
    """)
    Optional<Order> findByIdAndCustomerIdWithItemsAndTickets(@Param("orderId") Long orderId,
                                                             @Param("customerId") Long customerId);

    @Query("""
        select distinct o
        from Order o
        left join fetch o.items i
        left join fetch i.ticket t
        left join fetch t.ticketType tt
        where o.status = :status
          and exists (
              select oi.id
              from OrderItem oi
              join oi.ticket ot
              where oi.order = o
                and ot.holdExpiresAt is not null
                and ot.holdExpiresAt <= :now
          )
    """)
    List<Order> findPendingOrdersWithExpiredHolds(@Param("status") OrderStatus status, @Param("now") Date now);
}
