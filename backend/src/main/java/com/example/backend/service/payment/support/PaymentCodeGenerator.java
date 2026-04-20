package com.example.backend.service.payment.support;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PaymentCodeGenerator implements IPaymentCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int RANDOM_LENGTH = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder builder = new StringBuilder("PAY");
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int index = secureRandom.nextInt(ALPHABET.length());
            builder.append(ALPHABET.charAt(index));
        }
        return builder.toString();
    }
}
