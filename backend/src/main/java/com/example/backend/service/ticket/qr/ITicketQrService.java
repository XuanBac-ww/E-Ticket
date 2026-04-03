package com.example.backend.service.ticket.qr;

public interface ITicketQrService {

    byte[] generateQrCode(String content, int width, int height);
}
