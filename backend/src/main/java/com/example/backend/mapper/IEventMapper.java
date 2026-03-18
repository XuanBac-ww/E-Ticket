package com.example.backend.mapper;

import com.example.backend.dto.request.event.CreateEventRequest;
import com.example.backend.dto.response.event.EventResponse;
import com.example.backend.entities.Event;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IEventMapper {
    EventResponse toResponse(Event event);
    List<EventResponse> toResponseList(List<Event> events);
    Event createEntity(CreateEventRequest request);
}
