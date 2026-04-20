package com.example.backend.repository;

import com.example.backend.entities.TicketType;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITicketTypeRepository  extends IBaseEntityRepository<TicketType,Long> {

    List<TicketType> findByEventIdOrderByIdDesc(Long eventId);

    boolean existsTicketByName(String name);

    boolean existsByEventId(Long eventId);
}
