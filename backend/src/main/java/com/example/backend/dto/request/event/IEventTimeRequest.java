package com.example.backend.dto.request.event;

import java.util.Date;

public interface IEventTimeRequest {
    Date startTime();
    Date endTime();
}
