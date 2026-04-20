package com.example.backend.service.event;

import com.example.backend.dto.request.event.CreateEventRequest;
import com.example.backend.dto.request.event.EventSearchRequest;
import com.example.backend.dto.request.event.UpdateEventRequest;
import com.example.backend.dto.response.api.PageResponse;
import com.example.backend.dto.response.event.EventResponse;
import com.example.backend.entities.Event;
import com.example.backend.mapper.IEventMapper;
import com.example.backend.repository.IEventRepository;
import com.example.backend.repository.ITicketTypeRepository;
import com.example.backend.share.exception.AppException;
import com.example.backend.share.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService implements IEventService {

    private final IEventRepository eventRepository;
    private final IEventMapper eventMapper;
    private final ITicketTypeRepository ticketTypeRepository;

    @Override
    public PageResponse<EventResponse> getAllEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Event> events = eventRepository.findAll(pageable);

        return buildPageResponse(events, "Events retrieved successfully");
    }

    @Override
    public PageResponse<EventResponse> searchEvents(EventSearchRequest request, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String keyword = request.keyword() == null ? "" : request.keyword().trim();

        Page<Event> events = keyword.isEmpty()
                ? eventRepository.findAll(pageable)
                : eventRepository.searchEvents(keyword, pageable);

        return buildPageResponse(events, "Events searched successfully");
    }

    @Override
    public EventResponse findEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return eventMapper.toResponse(event);
    }

    @Override
    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        if (eventRepository.existsEventByTitle(request.title())) {
            throw new AppException("Event already exists");
        }

        Event event = eventMapper.createEntity(request);
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponse(savedEvent);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long eventId, UpdateEventRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        event.setLocation(request.location());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (ticketTypeRepository.existsByEventId(eventId)) {
            log.warn("Refusing to delete eventId={} because ticket types already exist", eventId);
            throw new AppException("Cannot delete an event that already has ticket types");
        }

        eventRepository.delete(event);
        log.info("Deleted eventId={}", eventId);
    }

    private PageResponse<EventResponse> buildPageResponse(Page<Event> events, String message) {
        List<EventResponse> eventResponses = eventMapper.toResponseList(events.getContent());

        return new PageResponse<>(
                200,
                true,
                message,
                eventResponses,
                events.getNumber(),
                events.getSize(),
                events.getTotalElements(),
                events.getTotalPages(),
                events.isLast()
        );
    }
}
