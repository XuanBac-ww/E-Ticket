package com.example.backend.repository;

import com.example.backend.entities.Ticket;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ITicketRepository extends IBaseEntityRepository<Ticket,Long> {

    @Query("select t from Ticket t join fetch t.ticketType where t.id in :ids")
    List<Ticket> findAllByIdInFetchTicketType(@Param("ids") List<Long> ids);

    @Query("""
        select t
        from Ticket t
        join fetch t.ticketType tt
        where t.id = :ticketId
    """)
    Optional<Ticket> findByIdInFetchTicketType(Long id);

}
