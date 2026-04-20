package com.example.backend.controller.customer.payment;

import com.example.backend.dto.response.payment.PaymentResponse;
import com.example.backend.security.CustomUserPrincipal;
import com.example.backend.service.payment.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping({"/orders/{orderId}/payments", "/orders/{orderId}/payments/qr"})
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> createPayment(@PathVariable Long orderId,
                                                         @AuthenticationPrincipal CustomUserPrincipal principal) {
        return new ResponseEntity<>(
                paymentService.createQrPayment(orderId, principal.getUserId()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/orders/{orderId}/payments/current")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> getCurrentPayment(@PathVariable Long orderId,
                                                             @AuthenticationPrincipal CustomUserPrincipal principal) {
        return new ResponseEntity<>(
                paymentService.getCurrentPayment(orderId, principal.getUserId()),
                HttpStatus.OK
        );
    }
}
