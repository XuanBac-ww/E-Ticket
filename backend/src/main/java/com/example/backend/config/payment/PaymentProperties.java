package com.example.backend.config.payment;

import com.example.backend.share.exception.AppException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment")
@Getter
@Setter
public class PaymentProperties {

    private String qrUrl;
    private String receiverName;

    public void validateConfiguration() {
        if (isBlank(qrUrl)) {
            throw new AppException("Payment QR configuration is incomplete");
        }
    }

    public String resolvePaymentQrUrl() {
        return qrUrl.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
