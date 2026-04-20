package com.example.backend.service.payment.notification;

import com.example.backend.config.mail.MailProperties;
import com.example.backend.dto.response.payment.notification.PaymentTicketsIssuedEvent;
import com.example.backend.entities.Order;
import com.example.backend.entities.OrderItem;
import com.example.backend.entities.Ticket;
import com.example.backend.repository.IOrderRepository;
import com.example.backend.service.ticket.qr.ITicketQrService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentTicketEmailListener {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;
    private final ResourceLoader resourceLoader;
    private final IOrderRepository orderRepository;
    private final ITicketQrService ticketQrService;
    private volatile String cachedTemplate;

    @Async("ticketQrExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTicketsIssued(PaymentTicketsIssuedEvent event) {
        if (!mailProperties.isEnabled()) {
            return;
        }

        if (event.customerEmail() == null || event.customerEmail().isBlank()) {
            log.warn("Skip sending ticket email because customer email is empty orderId={}", event.orderId());
            return;
        }

        Order order = orderRepository.findByIdWithItemsAndTickets(event.orderId()).orElse(null);
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            log.warn("Skip sending ticket email because order has no items orderId={}", event.orderId());
            return;
        }

        List<Ticket> tickets = order.getItems().stream()
                .map(OrderItem::getTicket)
                .filter(ticket -> ticket != null && ticket.getQrCodeHash() != null)
                .toList();

        if (tickets.isEmpty()) {
            log.warn("Skip sending ticket email because tickets are empty orderId={}", event.orderId());
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(event.customerEmail());
            helper.setFrom(mailProperties.getFrom());
            helper.setSubject(buildSubject(event.orderId()));
            helper.setText(buildHtmlBody(event, tickets), true);

            for (Ticket ticket : tickets) {
                byte[] qrBytes = ticketQrService.generateQrCode(ticket.getQrCodeHash(), 320, 320);
                String filename = "ticket-" + ticket.getId() + ".png";
                helper.addAttachment(filename, new ByteArrayResource(qrBytes), "image/png");
            }

            mailSender.send(mimeMessage);
        } catch (Exception ex) {
            log.error("Failed to send ticket email for orderId={}", event.orderId(), ex);
        }
    }

    private String buildSubject(Long orderId) {
        String prefix = mailProperties.getSubjectPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "[E-Ticket]";
        }
        return prefix + " Your tickets for order #" + orderId;
    }

    private String buildHtmlBody(PaymentTicketsIssuedEvent event, List<Ticket> tickets) {
        String template = loadTemplate();
        String ticketList = tickets.stream()
                .map(ticket -> String.valueOf(ticket.getId()))
                .collect(Collectors.joining(", "));
        return template
                .replace("{{customerName}}", safeValue(event.customerName(), "Customer"))
                .replace("{{orderId}}", String.valueOf(event.orderId()))
                .replace("{{ticketIds}}", ticketList);
    }

    private String loadTemplate() {
        if (cachedTemplate != null) {
            return cachedTemplate;
        }
        synchronized (this) {
            if (cachedTemplate != null) {
                return cachedTemplate;
            }
            cachedTemplate = readTemplate(mailProperties.getTicketTemplatePath());
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
            log.error("Failed to load ticket mail template {}", location, ex);
            return "<p>Your tickets are ready for order #" + "</p>";
        }
    }

    private String safeValue(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
