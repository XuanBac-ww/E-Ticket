package com.example.backend.controller;

import com.example.backend.dto.request.ticket.CreateTicketRequest;
import com.example.backend.dto.request.ticketType.UpdateTicketTypeRequest;
import com.example.backend.dto.response.Ticket.TicketResponse;
import com.example.backend.dto.response.ticketType.TicketTypeResponse;
import com.example.backend.service.ticket.ITicketService;
import com.example.backend.service.ticketType.ITicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final ITicketTypeService ticketTypeService;
    private final ITicketService ticketService;

    @GetMapping("/{id}")
    public ResponseEntity<TicketTypeResponse> findTicketTypeById(@PathVariable Long id) {
        TicketTypeResponse response = ticketTypeService.findTicketTypeById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketTypeResponse> updateTicketType(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateTicketTypeRequest request) {
        TicketTypeResponse response = ticketTypeService.updateTicketType(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTicketType(@PathVariable Long id) {
        ticketTypeService.deleteTicketType(id);
        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }

    @GetMapping("/{id}/tickets")
    public ResponseEntity<List<TicketResponse>> getTicketByTicketType(@PathVariable Long id) {
        List<TicketResponse> response = ticketService.getTicketByTicketType(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{id}/tickets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketResponse>> createTicketForTicketType(@PathVariable Long id,
                                                                          @Valid @RequestBody CreateTicketRequest request) {
        List<TicketResponse> response = ticketService.createTicketForTicketType(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
