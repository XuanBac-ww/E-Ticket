package com.example.backend.service.ticket;

import com.example.backend.dto.request.ticket.CreateTicketRequest;
import com.example.backend.dto.request.ticket.TicketCheckInRequest;
import com.example.backend.dto.response.Ticket.TicketCheckInValidationResponse;
import com.example.backend.entities.Event;
import com.example.backend.entities.Staff;
import com.example.backend.entities.Ticket;
import com.example.backend.entities.TicketType;
import com.example.backend.mapper.ITicketMapper;
import com.example.backend.repository.IOrderItemRepository;
import com.example.backend.repository.IStaffRepository;
import com.example.backend.repository.ITicketRepository;
import com.example.backend.repository.ITicketTypeRepository;
import com.example.backend.service.ticket.qr.ITicketQrService;
import com.example.backend.share.enums.TicketStatus;
import com.example.backend.share.exception.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private ITicketRepository ticketRepository;

    @Mock
    private ITicketMapper ticketMapper;

    @Mock
    private ITicketTypeRepository ticketTypeRepository;

    @Mock
    private IOrderItemRepository orderItemRepository;

    @Mock
    private ITicketQrService ticketQrService;

    @Mock
    private IStaffRepository staffRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createTicketForTicketTypeRejectsDuplicateSeatNumbersBeforeDatabaseLookup() {
        CreateTicketRequest request = new CreateTicketRequest(List.of("A1", "A1"));

        assertThatThrownBy(() -> ticketService.createTicketForTicketType(1L, request))
                .isInstanceOf(AppException.class)
                .hasMessage("Duplicate seat numbers are not allowed");

        verifyNoInteractions(ticketTypeRepository);
    }

    @Test
    void validateCheckInRejectsStaffAssignedToDifferentEvent() {
        Event event = new Event();
        event.setId(100L);
        event.setTitle("Concert");

        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);

        Ticket ticket = new Ticket();
        ticket.setTicketType(ticketType);
        ticket.setStatus(TicketStatus.SOLD);

        Staff staff = new Staff();
        staff.setId(9L);
        staff.setManagedEventId(200L);

        TicketCheckInValidationResponse expected = new TicketCheckInValidationResponse(
                false,
                "Staff is not assigned to this event",
                null,
                null,
                null,
                null
        );

        when(ticketRepository.findByQrCodeHashInFetchTicketTypeAndEvent("qr-hash")).thenReturn(Optional.of(ticket));
        when(staffRepository.findById(9L)).thenReturn(Optional.of(staff));
        when(ticketMapper.toCheckInValidationResponse(false, "Staff is not assigned to this event", ticket))
                .thenReturn(expected);

        TicketCheckInValidationResponse actual = ticketService.validateCheckIn(
                new TicketCheckInRequest("qr-hash"),
                9L
        );

        assertThat(actual).isSameAs(expected);
    }
}
