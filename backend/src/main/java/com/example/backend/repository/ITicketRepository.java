package com.example.backend.repository;

import com.example.backend.entities.Ticket;
import com.example.backend.entities.TicketType;
import com.example.backend.repository.abstraction.IBaseEntityRepository;
import com.example.backend.share.enums.TicketStatus;
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
    Optional<Ticket> findByIdInFetchTicketType(@Param("ticketId") Long ticketId);

    @Query("""
        select t
        from Ticket t
        join fetch t.ticketType tt
        where t.qrCodeHash = :qrCodeHash
    """)
    Optional<Ticket> findByQrCodeHashInFetchTicketType(@Param("qrCodeHash") String qrCodeHash);

    @Query("""
        select t
        from Ticket t
        join fetch t.ticketType tt
        join fetch tt.event e
        where t.qrCodeHash = :qrCodeHash
    """)
    Optional<Ticket> findByQrCodeHashInFetchTicketTypeAndEvent(@Param("qrCodeHash") String qrCodeHash);

    @Query("""
        select distinct t
        from Ticket t
        join fetch t.ticketType tt
        join OrderItem oi on oi.ticket = t
        join oi.order o
        where t.id = :ticketId
          and o.customer.id = :customerId
          and t.status in :statuses
    """)
    Optional<Ticket> findCustomerTicketById(@Param("ticketId") Long ticketId,
                                            @Param("customerId") Long customerId,
                                            @Param("statuses") List<TicketStatus> statuses);

    @Query("""
        select new com.example.backend.dto.response.report.TicketByEventReportResponse(
            e.id,
            e.title,
            count(t),
            sum(case when t.status in :soldStatuses then 1 else 0 end),
            sum(case when t.checkedIn = true then 1 else 0 end),
            sum(case when t.status in :remainingStatuses then 1 else 0 end)
        )
        from Ticket t
        join t.ticketType tt
        join tt.event e
        group by e.id, e.title
        order by e.id
    """)
    List<com.example.backend.dto.response.report.TicketByEventReportResponse> reportTicketByEvent(
            @Param("soldStatuses") List<TicketStatus> soldStatuses,
            @Param("remainingStatuses") List<TicketStatus> remainingStatuses
    );

    List<Ticket> findByTicketType(TicketType ticketType);

    List<Ticket> findByTicketTypeAndStatus(TicketType ticketType, TicketStatus ticketStatus);

    long countByTicketTypeAndStatus(TicketType ticketType, TicketStatus ticketStatus);

    long countByTicketType(TicketType ticketType);

    boolean existsByTicketTypeAndSeatNumberIn(TicketType ticketType, List<String> seatNumbers);

    @Query("""
        select distinct t
        from Ticket t
        join fetch t.ticketType tt
        join OrderItem oi on oi.ticket = t
        join oi.order o
        where o.customer.id = :customerId
          and t.status in :statuses
    """)
    List<Ticket> findByCustomerIdAndStatuses(@Param("customerId") Long customerId,
                                             @Param("statuses") List<TicketStatus> statuses);
}
