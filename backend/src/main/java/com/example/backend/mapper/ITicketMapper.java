package com.example.backend.mapper;

import com.example.backend.dto.response.Ticket.MyTicketResponse;
import com.example.backend.dto.response.Ticket.TicketCheckInResultResponse;
import com.example.backend.dto.response.Ticket.TicketCheckInValidationResponse;
import com.example.backend.dto.response.Ticket.TicketResponse;
import com.example.backend.entities.Ticket;
import com.example.backend.entities.TicketType;
import com.example.backend.share.enums.TicketStatus;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface ITicketMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "checkedInAt", ignore = true)
    @Mapping(target = "holdExpiresAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Ticket createEntity(TicketType ticketType,
                        String seatNumber,
                        String qrCodeHash,
                        TicketStatus status,
                        boolean checkedIn);

    @Mapping(target = "ticketTypeId", source = "ticketType.id")
    TicketResponse toResponse(Ticket ticket);

    @Mapping(target = "ticketTypeId", source = "ticketType.id")
    @Mapping(target = "qrUrl", expression = "java(\"/api/tickets/\" + ticket.getId() + \"/qr\")")
    MyTicketResponse toMyTicketResponse(Ticket ticket);

    @Mapping(target = "ticketId", source = "id")
    @Mapping(target = "eventId", source = "ticketType.event.id")
    @Mapping(target = "eventTitle", source = "ticketType.event.title")
    TicketCheckInResultResponse toCheckInResultResponse(Ticket ticket);

    @Mapping(target = "ticketId", source = "ticket.id")
    @Mapping(target = "eventId", source = "ticket.ticketType.event.id")
    @Mapping(target = "eventTitle", source = "ticket.ticketType.event.title")
    @Mapping(target = "seatNumber", source = "ticket.seatNumber")
    TicketCheckInValidationResponse toCheckInValidationResponse(boolean valid, String message, Ticket ticket);
}
