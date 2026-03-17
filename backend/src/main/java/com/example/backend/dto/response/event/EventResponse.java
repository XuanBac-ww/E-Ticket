package com.example.backend.dto.response.event;

import java.util.Date;

public record EventResponse(
        Long id,
        String title,
        String description,
        String location,
        Date startTime,
        Date endTime,
        String imageUrl
) {
}
