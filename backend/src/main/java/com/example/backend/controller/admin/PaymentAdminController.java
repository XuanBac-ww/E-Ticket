package com.example.backend.controller.admin;

import com.example.backend.dto.response.payment.PaymentResponse;
import com.example.backend.service.payment.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class PaymentAdminController {

    private final IPaymentService paymentService;

    @PatchMapping("/{orderId}/payments/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable Long orderId) {
        return new ResponseEntity<>(paymentService.confirmPayment(orderId), HttpStatus.OK);
    }
}
