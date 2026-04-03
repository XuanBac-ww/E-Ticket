package com.example.backend.mapper;

import com.example.backend.dto.response.Ticket.TicketResponse;
import com.example.backend.entities.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ITicketMapper {

    @Mapping(target = "ticketTypeId", source = "ticketType.id")
    TicketResponse toResponse(Ticket ticket);
}
