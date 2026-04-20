package com.example.backend.mapper;

import com.example.backend.dto.request.event.CreateEventRequest;
import com.example.backend.dto.response.event.EventResponse;
import com.example.backend.entities.Event;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface IEventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "ticketTypes", ignore = true)
    Event createEntity(CreateEventRequest request);

    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);
}
