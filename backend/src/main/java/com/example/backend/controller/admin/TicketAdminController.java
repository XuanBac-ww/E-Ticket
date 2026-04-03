package com.example.backend.controller.admin;

import com.example.backend.dto.request.ticket.TicketSummaryResponse;
import com.example.backend.dto.response.Ticket.TicketResponse;
import com.example.backend.service.ticket.ITicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ticket-management")
@RequiredArgsConstructor
public class TicketAdminController {

    private final ITicketService ticketService;

    // Xem báo cáo số lượng (Còn bao nhiêu, đã tạo bao nhiêu)
    @GetMapping("/summary/{ticketTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketSummaryResponse> getSummary(@PathVariable Long ticketTypeId) {
        return new ResponseEntity<>(ticketService.getTicketSummary(ticketTypeId), HttpStatus.OK);
    }

    // Xem danh sách các vé đã bán (Ai đã mua, số ghế nào)
    @GetMapping("/sold-list/{ticketTypeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketResponse>> getSoldList(@PathVariable Long ticketTypeId) {
        return new ResponseEntity<>(ticketService.getSoldTicketsDetails(ticketTypeId), HttpStatus.OK);
    }
}
