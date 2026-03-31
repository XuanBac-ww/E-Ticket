package com.example.backend.service.ticketType;


import com.example.backend.dto.request.ticketType.CreateTicketTypeRequest;
import com.example.backend.dto.request.ticketType.UpdateTicketTypeRequest;
import com.example.backend.dto.response.ticketType.TicketTypeResponse;
import com.example.backend.entities.Event;
import com.example.backend.entities.TicketType;
import com.example.backend.mapper.ITicketTypeMapper;
import com.example.backend.repository.IEventRepository;
import com.example.backend.repository.ITicketTypeRepository;
import com.example.backend.share.exception.AppException;
import com.example.backend.share.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketTypeService implements ITicketTypeService {

    private final ITicketTypeRepository ticketTypeRepository;
    private final ITicketTypeMapper ticketTypeMapper;
    private final IEventRepository eventRepository;


    @Override
    @Transactional
    public TicketTypeResponse createTicketTypeForEvent(Long eventId, CreateTicketTypeRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event Not Found"));

        if(ticketTypeRepository.existsTicketByName(request.name())) {
            throw new AppException("Ticket exists");
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
            throw new ResourceNotFoundException("Event Not Found");
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
                .orElseThrow(() -> new ResourceNotFoundException("TicketType Not Found"));
    }

    @Override
    @Transactional
    public TicketTypeResponse updateTicketType(Long id, UpdateTicketTypeRequest request) {
        TicketType ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType Not Found"));

        int soldQuantity = ticketType.getTotalQuantity() - ticketType.getRemainingQuantity();

        if (request.totalQuantity() < soldQuantity) {
            throw new AppException("Tổng số lượng vé không được nhỏ hơn số vé đã bán");
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
                .orElseThrow(() -> new ResourceNotFoundException("TicketType Not Found"));
        ticketTypeRepository.delete(ticketType);
    }


}
