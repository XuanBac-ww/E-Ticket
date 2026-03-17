package com.example.backend.service.ticketType;

import com.example.backend.dto.request.ticketType.CreateTicketTypeRequest;
import com.example.backend.dto.request.ticketType.UpdateTicketTypeRequest;
import com.example.backend.dto.response.ticketType.TicketTypeResponse;

import java.util.List;

public interface ITicketTypeService {
    TicketTypeResponse createTicketTypeForEvent(Long eventId,CreateTicketTypeRequest request);

    List<TicketTypeResponse> getTicketTypeForEvent(Long eventId);

    TicketTypeResponse findTicketTypeById(Long id);

    TicketTypeResponse updateTicketType(Long id, UpdateTicketTypeRequest request);

    void deleteTicketType(Long id);
}
