package com.example.backend.dto.request.event;

import com.example.backend.validation.annotation.ValidEventTime;
import jakarta.validation.constraints.*;

import java.util.Date;

@ValidEventTime
public record UpdateEventRequest(
        @NotBlank(message = "Địa điểm không được để trống")
        @Size(max = 255, message = "Địa điểm không được vượt quá 255 ký tự")
        String location,

        @NotNull(message = "Thời gian bắt đầu không được để trống")
        @FutureOrPresent(message = "Thời gian bắt đầu phải là hiện tại hoặc tương lai")
        Date startTime,

        @NotNull(message = "Thời gian kết thúc không được để trống")
        @Future(message = "Thời gian kết thúc phải ở tương lai")
        Date endTime
) implements IEventTimeRequest{
}
