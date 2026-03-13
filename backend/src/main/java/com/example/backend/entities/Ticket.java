package com.example.backend.entities;

import com.example.backend.entities.abstraction.BaseSoftDelete;
import com.example.backend.share.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class Ticket extends BaseSoftDelete {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @Column(name = "seat_number")
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.AVAILABLE;

    @Column(name = "qr_code_hash", unique = true)
    private String qrCodeHash;

    @Column(name = "is_checked_in")
    private boolean checkedIn = false;

    @Column(name = "checked_in_at")
    private Date checkedInAt;

    @Column(name = "hold_expires_at")
    private Date holdExpiresAt;

    @Version
    private Long version;
}
