package com.example.backend.validation.annotation;

import com.example.backend.validation.validator.EventTimeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EventTimeValidator.class)
@Documented
public @interface ValidEventTime {
    String message() default "Thời gian kết thúc phải sau thời gian bắt đầu";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
