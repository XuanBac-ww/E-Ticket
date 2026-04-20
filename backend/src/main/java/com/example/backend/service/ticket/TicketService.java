package com.example.backend.service.ticket;

import com.example.backend.dto.request.ticket.CreateTicketRequest;
import com.example.backend.dto.request.ticket.TicketCheckInRequest;
import com.example.backend.dto.request.ticket.TicketSummaryResponse;
import com.example.backend.dto.response.Ticket.MyTicketResponse;
import com.example.backend.dto.response.Ticket.TicketCheckInResultResponse;
import com.example.backend.dto.response.Ticket.TicketCheckInValidationResponse;
import com.example.backend.dto.response.Ticket.TicketResponse;
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
import com.example.backend.share.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Arrays;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketService implements ITicketService {

    private final ITicketRepository ticketRepository;
    private final ITicketMapper ticketMapper;
    private final ITicketTypeRepository ticketTypeRepository;
    private final IOrderItemRepository orderItemRepository;
    private final ITicketQrService ticketQrService;
    private final IStaffRepository staffRepository;

    @Override
    public TicketResponse findById(Long ticketId, Long userId) {
        log.debug("Fetching customer ticket detail ticketId={} userId={}", ticketId, userId);
        Ticket ticket = findOwnedIssuedTicket(ticketId, userId);
        return ticketMapper.toResponse(ticket);
    }

    private Ticket findOwnedIssuedTicket(Long ticketId, Long userId) {
        List<TicketStatus> issuedStatuses = Arrays.asList(TicketStatus.SOLD, TicketStatus.USED);
        return ticketRepository.findCustomerTicketById(ticketId, userId, issuedStatuses)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    @Override
    public List<TicketResponse> getTicketByTicketType(Long ticketTypeId) {
        log.debug("Fetching tickets by ticketTypeId={}", ticketTypeId);
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

        List<Ticket> tickets = ticketRepository.findByTicketType(ticketType);
        return tickets.stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<TicketResponse> createTicketForTicketType(Long ticketTypeId, CreateTicketRequest request) {
        log.info(
                "Creating tickets for ticketTypeId={} requestedSeatCount={}",
                ticketTypeId,
                request == null || request.seatNumber() == null ? 0 : request.seatNumber().size()
        );
        List<String> seatNumbers = normalizeSeatNumbers(request);

        validateDuplicateSeatInRequest(seatNumbers);

        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

        validateTicketCreationLimit(ticketType, seatNumbers.size());
        validateSeatNotExists(ticketType, seatNumbers);

        List<Ticket> newTickets = seatNumbers.stream()
                .map(seat -> buildNewTicket(ticketType, seat))
                .toList();

        List<Ticket> savedTickets = ticketRepository.saveAll(newTickets);
        log.info("Created {} tickets for ticketTypeId={}", savedTickets.size(), ticketTypeId);

        return savedTickets.stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketCheckInResultResponse checkIn(TicketCheckInRequest request, Long staffId) {
        if (request == null || request.qrCodeHash() == null || request.qrCodeHash().isBlank()) {
            throw new AppException("QR code is missing");
        }

        String qrCodeHash = request.qrCodeHash().trim();
        Ticket ticket = ticketRepository.findByQrCodeHashInFetchTicketTypeAndEvent(qrCodeHash)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        validateStaffCanCheckInTicket(staffId, ticket);

        String errorMessage = getCheckInError(ticket);
        if (errorMessage != null) {
            throw new AppException(errorMessage);
        }

        ticket.setCheckedIn(true);
        ticket.setCheckedInAt(new Date());
        ticket.setStatus(TicketStatus.USED);

        Ticket savedTicket = ticketRepository.save(ticket);
        return ticketMapper.toCheckInResultResponse(savedTicket);
    }

    @Override
    public TicketCheckInValidationResponse validateCheckIn(TicketCheckInRequest request, Long staffId) {
        if (request == null || request.qrCodeHash() == null || request.qrCodeHash().isBlank()) {
            return ticketMapper.toCheckInValidationResponse(false, "QR code is missing", null);
        }

        String qrCodeHash = request.qrCodeHash().trim();
        Ticket ticket = ticketRepository.findByQrCodeHashInFetchTicketTypeAndEvent(qrCodeHash).orElse(null);
        if (ticket == null) {
            return ticketMapper.toCheckInValidationResponse(false, "Ticket not found", null);
        }

        String staffErrorMessage = getStaffCheckInError(staffId, ticket);
        if (staffErrorMessage != null) {
            return ticketMapper.toCheckInValidationResponse(false, staffErrorMessage, ticket);
        }

        String errorMessage = getCheckInError(ticket);
        if (errorMessage != null) {
            return ticketMapper.toCheckInValidationResponse(false, errorMessage, ticket);
        }

        return ticketMapper.toCheckInValidationResponse(true, "OK", ticket);
    }

    @Override
    public byte[] getTicketByQr(Long ticketId, Long userId) {
        log.debug("Generating QR image for ticketId={} userId={}", ticketId, userId);
        Ticket ticket = findOwnedIssuedTicket(ticketId, userId);

        return ticketQrService.generateQrCode(ticket.getQrCodeHash(), 250, 250);
    }

    @Override
    public TicketSummaryResponse getTicketSummary(Long ticketTypeId) {
        log.debug("Fetching ticket summary ticketTypeId={}", ticketTypeId);
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

        long actualAvailable = ticketRepository.countByTicketTypeAndStatus(ticketType, TicketStatus.AVAILABLE);
        long totalCreated = ticketRepository.countByTicketType(ticketType);
        long totalSold = ticketRepository.countByTicketTypeAndStatus(ticketType, TicketStatus.SOLD);

        return new TicketSummaryResponse(
                ticketType.getName(),
                ticketType.getTotalQuantity(),
                ticketType.getRemainingQuantity(),
                actualAvailable,
                totalCreated,
                totalSold
        );
    }

    @Override
    public List<MyTicketResponse> getMyTickets(Long userId) {
        List<TicketStatus> statuses = Arrays.asList(TicketStatus.SOLD, TicketStatus.USED);
        List<Ticket> tickets = ticketRepository.findByCustomerIdAndStatuses(userId, statuses);
        return tickets.stream()
                .map(ticketMapper::toMyTicketResponse)
                .toList();
    }

    @Override
    public List<TicketResponse> getSoldTicketsDetails(Long ticketTypeId) {
        log.debug("Fetching sold ticket details ticketTypeId={}", ticketTypeId);
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

        List<Ticket> soldTickets = ticketRepository.findByTicketTypeAndStatus(ticketType, TicketStatus.SOLD);

        return soldTickets.stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        TicketType ticketType = ticket.getTicketType();

        if (isEventOpenForSale(ticketType)) {
            log.warn(
                    "Refusing to delete ticketId={} because eventId={} is still open for sale",
                    ticketId,
                    ticketType.getEvent().getId()
            );
            throw new AppException("Cannot delete a ticket while the event is open for sale or has not ended");
        }

        if (ticket.getStatus() != TicketStatus.AVAILABLE) {
            log.warn(
                    "Refusing to delete ticketId={} because status={}",
                    ticketId,
                    ticket.getStatus()
            );
            throw new AppException("Only tickets in AVAILABLE status can be deleted");
        }

        if (orderItemRepository.existsByTicketId(ticketId)) {
            log.warn("Refusing to delete ticketId={} because order item history already exists", ticketId);
            throw new AppException("Cannot delete a ticket that already belongs to an order");
        }

        ticketRepository.delete(ticket);
        log.info("Deleted ticketId={}", ticketId);
    }

    private List<String> normalizeSeatNumbers(CreateTicketRequest request) {
        if (request == null || request.seatNumber() == null || request.seatNumber().isEmpty()) {
            throw new AppException("Seat number list must not be empty");
        }

        List<String> seatNumbers = request.seatNumber().stream()
                .map(String::trim)
                .filter(seat -> !seat.isBlank())
                .toList();

        if (seatNumbers.isEmpty()) {
            throw new AppException("Seat number list is invalid");
        }

        return seatNumbers;
    }

    private void validateDuplicateSeatInRequest(List<String> seatNumbers) {
        long distinctCount = seatNumbers.stream().distinct().count();
        if (distinctCount != seatNumbers.size()) {
            throw new AppException("Duplicate seat numbers are not allowed");
        }
    }

    private void validateTicketCreationLimit(TicketType ticketType, int requestedSize) {
        long totalCreated = ticketRepository.countByTicketType(ticketType);

        if (totalCreated + requestedSize > ticketType.getTotalQuantity()) {
            throw new AppException("Requested ticket quantity exceeds the ticket type limit");
        }
    }

    private void validateSeatNotExists(TicketType ticketType, List<String> seatNumbers) {
        boolean exists = ticketRepository.existsByTicketTypeAndSeatNumberIn(ticketType, seatNumbers);
        if (exists) {
            throw new AppException("One or more seat numbers already exist");
        }
    }

    private Ticket buildNewTicket(TicketType ticketType, String seat) {
        return ticketMapper.createEntity(
                ticketType,
                seat,
                UUID.randomUUID().toString(),
                TicketStatus.AVAILABLE,
                false
        );
    }

    private String getCheckInError(Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.USED || ticket.isCheckedIn()) {
            return "Ticket already used";
        }

        if (ticket.getStatus() != TicketStatus.SOLD) {
            if (ticket.getStatus() == TicketStatus.HOLDING || ticket.getStatus() == TicketStatus.AVAILABLE) {
                return "Ticket has not been paid";
            }
            if (ticket.getStatus() == TicketStatus.CANCELLED) {
                return "Ticket has been cancelled";
            }
            return "Ticket is not valid for check-in";
        }

        return null;
    }

    private void validateStaffCanCheckInTicket(Long staffId, Ticket ticket) {
        String errorMessage = getStaffCheckInError(staffId, ticket);
        if (errorMessage != null) {
            throw new AppException(errorMessage);
        }
    }

    private String getStaffCheckInError(Long staffId, Ticket ticket) {
        Staff staff = staffRepository.findById(staffId)
                .orElse(null);

        if (staff == null || staff.getManagedEventId() == null) {
            return "Staff is not assigned to this event";
        }

        Long eventId = getEventId(ticket);
        if (!Objects.equals(staff.getManagedEventId(), eventId)) {
            return "Staff is not assigned to this event";
        }

        return null;
    }

    private Long getEventId(Ticket ticket) {
        if (ticket == null || ticket.getTicketType() == null || ticket.getTicketType().getEvent() == null) {
            return null;
        }
        return ticket.getTicketType().getEvent().getId();
    }

    private boolean isEventOpenForSale(TicketType ticketType) {
        return ticketType != null
                && ticketType.getEvent() != null
                && ticketType.getEvent().getEndTime() != null
                && ticketType.getEvent().getEndTime().after(new Date());
    }
}
