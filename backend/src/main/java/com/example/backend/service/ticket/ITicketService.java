package com.example.backend.service.ticket;

import com.example.backend.dto.request.ticket.CreateTicketRequest;
import com.example.backend.dto.request.ticket.TicketCheckInRequest;
import com.example.backend.dto.request.ticket.TicketSummaryResponse;
import com.example.backend.dto.response.Ticket.MyTicketResponse;
import com.example.backend.dto.response.Ticket.TicketCheckInResultResponse;
import com.example.backend.dto.response.Ticket.TicketCheckInValidationResponse;
import com.example.backend.dto.response.Ticket.TicketResponse;

import java.util.List;

public interface ITicketService {
    TicketResponse findById(Long ticketId, Long userId);

    TicketCheckInResultResponse checkIn(TicketCheckInRequest request, Long staffId);

    TicketCheckInValidationResponse validateCheckIn(TicketCheckInRequest request, Long staffId);

    List<TicketResponse> getTicketByTicketType(Long ticketTypeId);

    List<TicketResponse> createTicketForTicketType(Long ticketTypeId, CreateTicketRequest request);

    List<TicketResponse> getSoldTicketsDetails(Long ticketTypeId);

    byte[] getTicketByQr(Long ticketId, Long userId);

    TicketSummaryResponse getTicketSummary(Long ticketTypeId);

    List<MyTicketResponse> getMyTickets(Long userId);

    void deleteTicket(Long ticketId);
}
