package com.example.backend.service.payment.notification;

import com.example.backend.config.mail.MailProperties;
import com.example.backend.dto.response.payment.notification.PaymentConfirmedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEmailListener {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final ResourceLoader resourceLoader;
    private volatile String cachedTemplate;

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
        if (!mailProperties.isEnabled()) {
            return;
        }

        if (event.customerEmail() == null || event.customerEmail().isBlank()) {
            log.warn("Skip sending payment email because customer email is empty orderId={}", event.orderId());
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(event.customerEmail());
            helper.setFrom(mailProperties.getFrom());
            helper.setSubject(buildSubject(event));
            helper.setText(buildHtmlBody(event), true);
            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            log.error("Failed to send payment confirmation email for orderId={}", event.orderId(), ex);
        }
    }

    private String buildSubject(PaymentConfirmedEvent event) {
        String prefix = mailProperties.getSubjectPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "[E-Ticket]";
        }
        return prefix + " Payment confirmed for order #" + event.orderId();
    }

    private String buildHtmlBody(PaymentConfirmedEvent event) {
        String template = loadTemplate();
        return template
                .replace("{{customerName}}", safeValue(event.customerName(), "Customer"))
                .replace("{{orderId}}", String.valueOf(event.orderId()))
                .replace("{{amount}}", formatAmount(event.amount()))
                .replace("{{paymentCode}}", safeValue(event.paymentCode(), ""))
                .replace("{{transferContent}}", safeValue(event.transferContent(), ""))
                .replace("{{paymentDate}}", formatPaymentDate(event.paymentDate()));
    }

    private String loadTemplate() {
        if (cachedTemplate != null) {
            return cachedTemplate;
        }
        synchronized (this) {
            if (cachedTemplate != null) {
                return cachedTemplate;
            }
            cachedTemplate = readTemplate(mailProperties.getTemplatePath());
            return cachedTemplate;
        }
    }

    private String readTemplate(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            log.error("Failed to load mail template {}", location, ex);
            return "<p>Payment confirmed.</p>";
        }
    }

    private String safeValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String formatAmount(java.math.BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }

    private String formatPaymentDate(java.util.Date paymentDate) {
        if (paymentDate == null) {
            return "-";
        }
        String pattern = mailProperties.getDateTimePattern();
        if (pattern == null || pattern.isBlank()) {
            pattern = "yyyy-MM-dd HH:mm";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return Instant.ofEpochMilli(paymentDate.getTime())
                .atZone(ZoneId.systemDefault())
                .format(formatter);
    }
}
