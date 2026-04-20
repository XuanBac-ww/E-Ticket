package com.example.backend.service.ticketType;

import com.example.backend.dto.request.ticketType.CreateTicketTypeRequest;
import com.example.backend.dto.request.ticketType.UpdateTicketTypeRequest;
import com.example.backend.dto.response.ticketType.TicketTypeResponse;
import com.example.backend.entities.Event;
import com.example.backend.entities.TicketType;
import com.example.backend.mapper.ITicketTypeMapper;
import com.example.backend.repository.IEventRepository;
import com.example.backend.repository.ITicketRepository;
import com.example.backend.repository.ITicketTypeRepository;
import com.example.backend.share.exception.AppException;
import com.example.backend.share.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketTypeService implements ITicketTypeService {

    private final ITicketTypeRepository ticketTypeRepository;
    private final ITicketTypeMapper ticketTypeMapper;
    private final IEventRepository eventRepository;
    private final ITicketRepository ticketRepository;

    @Override
    @Transactional
    public TicketTypeResponse createTicketTypeForEvent(Long eventId, CreateTicketTypeRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (ticketTypeRepository.existsTicketByName(request.name())) {
            throw new AppException("Ticket type already exists");
        }

        TicketType ticketType = ticketTypeMapper.createEntity(request);
        ticketType.setEvent(event);
        ticketType.setRemainingQuantity(request.totalQuantity());
        ticketTypeRepository.save(ticketType);
        return ticketTypeMapper.toResponse(ticketType);
    }

    @Override
    public List<TicketTypeResponse> getTicketTypeForEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found");
        }

        return ticketTypeRepository.findByEventIdOrderByIdDesc(eventId)
                .stream()
                .map(ticketTypeMapper::toResponse)
                .toList();
    }

    @Override
    public TicketTypeResponse findTicketTypeById(Long id) {
        return ticketTypeRepository.findById(id)
                .map(ticketTypeMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));
    }

    @Override
    @Transactional
    public TicketTypeResponse updateTicketType(Long id, UpdateTicketTypeRequest request) {
        TicketType ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

        int soldQuantity = ticketType.getTotalQuantity() - ticketType.getRemainingQuantity();

        if (request.totalQuantity() < soldQuantity) {
            throw new AppException("Total ticket quantity cannot be less than the number of tickets sold");
        }
        ticketType.setPrice(request.price());
        ticketType.setTotalQuantity(request.totalQuantity());
        ticketType.setRemainingQuantity(request.totalQuantity() - soldQuantity);
        ticketTypeRepository.save(ticketType);
        return ticketTypeMapper.toResponse(ticketType);
    }

    @Override
    @Transactional
    public void deleteTicketType(Long id) {
        TicketType ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

        if (isEventOpenForSale(ticketType.getEvent())) {
            log.warn(
                    "Refusing to delete ticketTypeId={} because eventId={} is still open for sale",
                    id,
                    ticketType.getEvent().getId()
            );
            throw new AppException("Cannot delete a ticket type while the event is open for sale or has not ended");
        }

        if (ticketRepository.countByTicketType(ticketType) > 0) {
            log.warn("Refusing to delete ticketTypeId={} because tickets already exist", id);
            throw new AppException("Cannot delete a ticket type that already has tickets");
        }

        ticketTypeRepository.delete(ticketType);
        log.info("Deleted ticketTypeId={}", id);
    }

    private boolean isEventOpenForSale(Event event) {
        return event != null
                && event.getEndTime() != null
                && event.getEndTime().after(new Date());
    }
}
