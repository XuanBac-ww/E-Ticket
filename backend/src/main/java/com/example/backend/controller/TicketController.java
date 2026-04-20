package com.example.backend.controller;

import com.example.backend.dto.request.ticket.TicketCheckInRequest;
import com.example.backend.dto.response.Ticket.MyTicketResponse;
import com.example.backend.dto.response.Ticket.TicketCheckInResultResponse;
import com.example.backend.dto.response.Ticket.TicketCheckInValidationResponse;
import com.example.backend.dto.response.Ticket.TicketResponse;
import com.example.backend.security.CustomUserPrincipal;
import com.example.backend.service.ticket.ITicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final ITicketService ticketService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long ticketId,
                                                        @AuthenticationPrincipal CustomUserPrincipal principal) {
        TicketResponse response = ticketService.findById(ticketId, principal.getUserId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my")
    public ResponseEntity<List<MyTicketResponse>> getMyTickets(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<MyTicketResponse> response = ticketService.getMyTickets(principal.getUserId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/check-in")
    public ResponseEntity<TicketCheckInResultResponse> checkIn(@Valid @RequestBody TicketCheckInRequest request,
                                                               @AuthenticationPrincipal CustomUserPrincipal principal) {
        TicketCheckInResultResponse response = ticketService.checkIn(request, principal.getUserId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/check-in/validate")
    public ResponseEntity<TicketCheckInValidationResponse> validateCheckIn(
            @Valid @RequestBody TicketCheckInRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        TicketCheckInValidationResponse response = ticketService.validateCheckIn(request, principal.getUserId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{ticketId}/qr")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<byte[]> getTicketQr(@PathVariable Long ticketId,
                                              @AuthenticationPrincipal CustomUserPrincipal principal) {
        byte[] response = ticketService.getTicketByQr(ticketId, principal.getUserId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @DeleteMapping("/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTicket(@PathVariable Long ticketId) {
        ticketService.deleteTicket(ticketId);
        return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
    }
}
