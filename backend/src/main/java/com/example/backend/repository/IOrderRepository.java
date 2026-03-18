package com.example.backend.repository;

import com.example.backend.entities.Order;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IOrderRepository extends IBaseEntityRepository<Order,Long> {
    @Query("""
        select distinct o
        from Order o
        left join fetch o.items i
        left join fetch i.ticket t
        left join fetch t.ticketType tt
        where o.id = :orderId
    """)
    Optional<Order> findByIdWithItemsAndTickets(Long orderId);
}
