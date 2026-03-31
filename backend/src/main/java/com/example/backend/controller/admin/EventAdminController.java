package com.example.backend.controller.admin;

import com.example.backend.dto.request.event.CreateEventRequest;
import com.example.backend.dto.request.event.UpdateEventRequest;
import com.example.backend.dto.response.event.EventResponse;
import com.example.backend.service.event.IEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final IEventService eventService;

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createEvent(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long eventId,
                                                     @Valid @RequestBody UpdateEventRequest request) {
        EventResponse response = eventService.updateEvent(eventId,request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteEventById(@PathVariable Long eventId) {
        eventService.deleteEventById(eventId);
        return new ResponseEntity<>("Delete Successfully",HttpStatus.OK);
    }



}

