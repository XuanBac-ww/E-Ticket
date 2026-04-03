package com.example.backend.service.ticket;

import com.example.backend.dto.request.ticket.CreateTicketRequest;
import com.example.backend.dto.request.ticket.TicketCheckInRequest;
import com.example.backend.dto.request.ticket.TicketSummaryResponse;
import com.example.backend.dto.response.Ticket.TicketResponse;

import java.util.List;

public interface ITicketService {
    TicketResponse findById(Long ticketId);

    TicketResponse checkIn(TicketCheckInRequest request);

    List<TicketResponse> getTicketByTicketType(Long ticketTypeId);

    List<TicketResponse> createTicketForTicketType(Long ticketTypeId, CreateTicketRequest request);

    List<TicketResponse> getSoldTicketsDetails(Long ticketTypeId);

    byte[] getTicketByQr(Long ticketId);

    TicketSummaryResponse getTicketSummary(Long ticketTypeId);
}
