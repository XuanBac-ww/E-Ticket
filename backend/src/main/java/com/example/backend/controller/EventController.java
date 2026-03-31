package com.example.backend.controller;

import com.example.backend.dto.request.event.EventSearchRequest;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.dto.response.event.EventResponse;
import com.example.backend.service.event.IEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final IEventService eventService;


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


}
