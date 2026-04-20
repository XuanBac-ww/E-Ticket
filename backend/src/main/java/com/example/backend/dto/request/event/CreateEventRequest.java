package com.example.backend.dto.request.event;

import com.example.backend.validation.annotation.ValidEventTime;
import jakarta.validation.constraints.*;

import java.util.Date;

@ValidEventTime
public record CreateEventRequest(
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Description must not be blank")
        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @NotBlank(message = "Location must not be blank")
        @Size(max = 255, message = "Location must not exceed 255 characters")
        String location,

        @NotNull(message = "Start time must not be null")
        @FutureOrPresent(message = "Start time must be in the present or future")
        Date startTime,

        @NotNull(message = "End time must not be null")
        @Future(message = "End time must be in the future")
        Date endTime,

        @Size(max = 1000, message = "Image URL must not exceed 1000 characters")
        String imageUrl
) implements IEventTimeRequest {
}
