package com.example.backend.dto.request.ticket;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateTicketRequest {
    @NotEmpty(message = "Danh sách ghế không được để trống")
    private List<String> seatNumber;
}
