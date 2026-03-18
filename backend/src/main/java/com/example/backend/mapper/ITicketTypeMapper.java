package com.example.backend.mapper;

import com.example.backend.dto.request.ticketType.CreateTicketTypeRequest;
import com.example.backend.dto.response.ticketType.TicketTypeResponse;
import com.example.backend.entities.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ITicketTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "remainingQuantity", ignore = true)
    TicketType createEntity(CreateTicketTypeRequest request);

    @Mapping(target = "eventId", source = "event.id")
    TicketTypeResponse toResponse(TicketType ticketType);
}
