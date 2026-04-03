package com.example.backend.service.ticket;


import com.example.backend.dto.request.ticket.CreateTicketRequest;
import com.example.backend.dto.request.ticket.TicketCheckInRequest;
import com.example.backend.dto.request.ticket.TicketSummaryResponse;
import com.example.backend.dto.response.Ticket.TicketResponse;
import com.example.backend.entities.Ticket;
import com.example.backend.entities.TicketType;
import com.example.backend.mapper.ITicketMapper;
import com.example.backend.repository.ITicketRepository;
import com.example.backend.repository.ITicketTypeRepository;
import com.example.backend.service.ticket.qr.ITicketQrService;
import com.example.backend.share.enums.TicketStatus;
import com.example.backend.share.exception.AppException;
import com.example.backend.share.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService  implements ITicketService {

    private final ITicketRepository ticketRepository;
    private final ITicketMapper ticketMapper;
    private final ITicketTypeRepository ticketTypeRepository;
    private final ITicketQrService ticketQrService;

    @Override
    public TicketResponse findById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->  new ResourceNotFoundException("Không tìm thấy vé"));
        return ticketMapper.toResponse(ticket);
    }

    @Override
    public List<TicketResponse> getTicketByTicketType(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Ticket Type"));

        List<Ticket> tickets = ticketRepository.findByTicketType(ticketType);
        return tickets.stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional()
    public List<TicketResponse> createTicketForTicketType(Long ticketTypeId, CreateTicketRequest request) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Ticket Type"));

        List<String> seatNumbers = normalizeSeatNumbers(request);

        validateDuplicateSeatInRequest(seatNumbers);
        validateTicketCreationLimit(ticketType, seatNumbers.size());
        validateSeatNotExists(ticketType, seatNumbers);

        List<Ticket> newTickets = seatNumbers.stream()
                .map(seat -> buildNewTicket(ticketType, seat))
                .toList();

        List<Ticket> savedTickets = ticketRepository.saveAll(newTickets);

        return savedTickets.stream()
                .map(ticketMapper::toResponse)
                .toList();
    }


    @Override
    @Transactional
    public TicketResponse checkIn(TicketCheckInRequest request) {
        return null;
    }

    @Override
    public byte[] getTicketByQr(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ticket"));

        return ticketQrService.generateQrCode(ticket.getQrCodeHash(), 250, 250);
    }

    @Override
    public TicketSummaryResponse getTicketSummary(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hạng vé này!"));

        long actualAvailable = ticketRepository.countByTicketTypeAndStatus(ticketType, TicketStatus.AVAILABLE);
        long totalCreated = ticketRepository.countByTicketType(ticketType);
        long totalSold = ticketRepository.countByTicketTypeAndStatus(ticketType, TicketStatus.SOLD);

        return TicketSummaryResponse.builder()
                .ticketTypeName(ticketType.getName())
                .totalLimit(ticketType.getTotalQuantity())
                .remainingInField(ticketType.getRemainingQuantity())
                .actualAvailable(actualAvailable)
                .totalCreated(totalCreated)
                .totalSold(totalSold)
                .build();
    }

    @Override
    public List<TicketResponse> getSoldTicketsDetails(Long ticketTypeId) {
        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hạng vé không tồn tại"));

        List<Ticket> soldTickets = ticketRepository.findByTicketTypeAndStatus(ticketType, TicketStatus.SOLD);

        return soldTickets.stream().map(ticket -> new TicketResponse(
                ticket.getId(),
                ticketType.getId(),
                ticket.getSeatNumber(),
                ticket.getStatus(),
                ticket.isCheckedIn(),
                ticket.getCheckedInAt()
        )).toList();
    }


    private List<String> normalizeSeatNumbers(CreateTicketRequest request) {
        if (request == null || request.getSeatNumber() == null || request.getSeatNumber().isEmpty()) {
            throw new IllegalArgumentException("Danh sách số ghế không được để trống");
        }

        List<String> seatNumbers = request.getSeatNumber().stream()
                .map(String::trim)
                .filter(seat -> !seat.isBlank())
                .toList();

        if (seatNumbers.isEmpty()) {
            throw new IllegalArgumentException("Danh sách số ghế không hợp lệ");
        }

        return seatNumbers;
    }

    private void validateDuplicateSeatInRequest(List<String> seatNumbers) {
        long distinctCount = seatNumbers.stream().distinct().count();
        if (distinctCount != seatNumbers.size()) {
            throw new AppException("Danh sách ghế có ghế bị trùng");
        }
    }

    private void validateTicketCreationLimit(TicketType ticketType, int requestedSize) {
        long totalCreated = ticketRepository.countByTicketType(ticketType);

        if (totalCreated + requestedSize > ticketType.getTotalQuantity()) {
            throw new AppException("Số lượng vé tạo vượt quá giới hạn của hạng vé");
        }
    }

    private void validateSeatNotExists(TicketType ticketType, List<String> seatNumbers) {
        boolean exists = ticketRepository.existsByTicketTypeAndSeatNumberIn(ticketType, seatNumbers);
        if (exists) {
            throw new AppException("Một hoặc nhiều số ghế đã tồn tại");
        }
    }

    private Ticket buildNewTicket(TicketType ticketType, String seat) {
        Ticket ticket = new Ticket();
        ticket.setSeatNumber(seat);
        ticket.setTicketType(ticketType);
        ticket.setCheckedIn(false);
        ticket.setQrCodeHash(UUID.randomUUID().toString());

        // tạo qr cho ticket ở đây
        return ticket;
    }
}
