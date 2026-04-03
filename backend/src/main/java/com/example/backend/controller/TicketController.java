package com.example.backend.controller;

import com.example.backend.dto.request.ticket.TicketCheckInRequest;
import com.example.backend.dto.response.Ticket.TicketResponse;
import com.example.backend.service.ticket.ITicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final ITicketService ticketService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId) {
        TicketResponse response = ticketService.findById(ticketId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/check-in")
    public ResponseEntity<TicketResponse> checkIn(@Valid @RequestBody TicketCheckInRequest request) {
        TicketResponse response = ticketService.checkIn(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{ticketId}/qr")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<byte[]> getTicketQr(@PathVariable Long ticketId) {
        byte[] response = ticketService.getTicketByQr(ticketId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }


}
