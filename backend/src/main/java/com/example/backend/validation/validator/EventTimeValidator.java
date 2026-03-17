package com.example.backend.validation.validator;

import com.example.backend.dto.request.event.IEventTimeRequest;
import com.example.backend.validation.annotation.ValidEventTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventTimeValidator implements ConstraintValidator<ValidEventTime, IEventTimeRequest> {

    @Override
    public boolean isValid(IEventTimeRequest request, ConstraintValidatorContext context) {
        if (request == null || request.startTime() == null || request.endTime() == null) {
            return true;
        }
        return request.endTime().after(request.startTime());
    }
}