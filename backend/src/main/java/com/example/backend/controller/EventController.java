package com.example.backend.controller;

import com.example.backend.dto.request.event.EventSearchRequest;
import com.example.backend.dto.request.ticketType.CreateTicketTypeRequest;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.dto.response.event.EventResponse;
import com.example.backend.dto.response.ticketType.TicketTypeResponse;
import com.example.backend.service.event.IEventService;
import com.example.backend.service.ticketType.ITicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final IEventService eventService;
    private final ITicketTypeService ticketTypeService;


    @GetMapping("")
    public ResponseEntity<PageResponse<EventResponse>> getAllEvents(@RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        PageResponse<EventResponse> response = eventService.getAllEvents(page,size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<EventResponse>> searchEvents(@Valid @RequestBody EventSearchRequest request,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        PageResponse<EventResponse> response = eventService.searchEvents(request,page,size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> findEventById(@PathVariable Long eventId) {
        EventResponse response = eventService.findEventById(eventId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }



    // Ticket Type
    @PostMapping("/{eventId}/ticket-types")
    public ResponseEntity<TicketTypeResponse> createTicketTypeForEvent(@Valid @RequestBody CreateTicketTypeRequest request,
                                                                       @PathVariable Long eventId) {
        TicketTypeResponse response = ticketTypeService.createTicketTypeForEvent(eventId,request);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @GetMapping("/{eventId}/ticket-types")
    public ResponseEntity<List<TicketTypeResponse>> getTicketTypeForEvent(@PathVariable Long eventId) {
        List<TicketTypeResponse> response = ticketTypeService.getTicketTypeForEvent(eventId);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }



}
