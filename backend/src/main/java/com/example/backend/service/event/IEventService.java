package com.example.backend.service.event;

import com.example.backend.dto.request.event.CreateEventRequest;
import com.example.backend.dto.request.event.EventSearchRequest;
import com.example.backend.dto.request.event.UpdateEventRequest;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.dto.response.event.EventResponse;

public interface IEventService {
    PageResponse<EventResponse> getAllEvents(int page, int size);

    PageResponse<EventResponse> searchEvents(EventSearchRequest request, int page, int size);

    EventResponse findEventById(Long eventId);

    EventResponse createEvent(CreateEventRequest request);

    EventResponse updateEvent(Long eventId, UpdateEventRequest request);

    void deleteEventById(Long eventId);
}
