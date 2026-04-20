package com.example.backend.dto.request.event;

import com.example.backend.validation.annotation.ValidEventTime;
import jakarta.validation.constraints.*;

import java.util.Date;

@ValidEventTime
public record UpdateEventRequest(
        @NotBlank(message = "Location must not be blank")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        @NotNull(message = "Start time must not be null")
        @FutureOrPresent(message = "Start time must be in the present or future")
        Date startTime,

        @NotNull(message = "End time must not be null")
        @Future(message = "End time must be in the future")
        Date endTime
) implements IEventTimeRequest {
}
