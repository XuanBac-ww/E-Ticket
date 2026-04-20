package com.example.backend.entities;

import com.example.backend.entities.abstraction.BaseEntity;
import com.example.backend.share.enums.PaymentMethod;
import com.example.backend.share.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_code", nullable = false, unique = true, length = 100)
    private String paymentCode;

    @Column(name = "qr_url", nullable = false, length = 1000)
    private String qrUrl;

    @Column(name = "receiver_name", length = 255)
    private String receiverName;

    @Column(name = "transfer_content", nullable = false, length = 255)
    private String transferContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentStatus status;

    @Column(name = "expired_at")
    private Date expiredAt;

    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(columnDefinition = "TEXT")
    private String message;
}
