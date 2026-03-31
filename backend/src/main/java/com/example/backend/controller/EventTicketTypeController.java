package com.example.backend.controller;

import com.example.backend.dto.request.ticketType.CreateTicketTypeRequest;
import com.example.backend.dto.response.ticketType.TicketTypeResponse;
import com.example.backend.service.ticketType.ITicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/ticket-types")
@RequiredArgsConstructor
public class EventTicketTypeController {

    private final ITicketTypeService ticketTypeService;

    @PostMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketTypeResponse> createTicketTypeForEvent(@Valid @RequestBody CreateTicketTypeRequest request,
                                                                       @PathVariable Long eventId) {
        TicketTypeResponse response = ticketTypeService.createTicketTypeForEvent(eventId,request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<TicketTypeResponse>> getTicketTypeForEvent(@PathVariable Long eventId) {
        List<TicketTypeResponse> response = ticketTypeService.getTicketTypeForEvent(eventId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }
}
