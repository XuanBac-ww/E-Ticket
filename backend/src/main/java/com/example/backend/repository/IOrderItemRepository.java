package com.example.backend.repository;

import com.example.backend.entities.OrderItem;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IOrderItemRepository extends IBaseEntityRepository<OrderItem,Long> {
    Optional<OrderItem> findByIdAndOrderId(Long itemId, Long orderId);

    boolean existsByTicketId(Long ticketId);
}
