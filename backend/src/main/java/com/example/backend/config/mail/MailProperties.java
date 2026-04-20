package com.example.backend.config.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
@Getter
@Setter
public class MailProperties {
    private boolean enabled = false;
    private String from = "no-reply@e-ticket.local";
    private String subjectPrefix = "[E-Ticket]";
    private String templatePath = "classpath:templates/mail/payment-confirmed.html";
    private String dateTimePattern = "yyyy-MM-dd HH:mm";
    private String ticketTemplatePath = "classpath:templates/mail/payment-tickets.html";
}
