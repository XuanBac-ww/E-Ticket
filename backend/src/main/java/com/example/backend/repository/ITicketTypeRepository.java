package com.example.backend.repository;

import com.example.backend.dto.response.ticketType.TicketTypeResponse;
import com.example.backend.entities.TicketType;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ITicketTypeRepository  extends IBaseEntityRepository<TicketType,Long> {

    List<TicketType> findByEventIdOrderByIdDesc(Long eventId);

    boolean existsTicketByName(String name);
}
