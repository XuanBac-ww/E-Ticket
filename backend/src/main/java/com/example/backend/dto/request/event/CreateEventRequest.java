package com.example.backend.dto.request.event;

import com.example.backend.validation.annotation.ValidEventTime;
import jakarta.validation.constraints.*;

import java.util.Date;

@ValidEventTime
public record CreateEventRequest(
        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
        String title,

        @NotBlank(message = "Mô tả không được để trống")
        @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
        String description,

        @NotBlank(message = "Địa điểm không được để trống")
        @Size(max = 255, message = "Địa điểm không được vượt quá 255 ký tự")
        String location,

        @NotNull(message = "Thời gian bắt đầu không được để trống")
        @FutureOrPresent(message = "Thời gian bắt đầu phải là hiện tại hoặc tương lai")
        Date startTime,

        @NotNull(message = "Thời gian kết thúc không được để trống")
        @Future(message = "Thời gian kết thúc phải ở tương lai")
        Date endTime,

        @Size(max = 1000, message = "Đường dẫn ảnh không được vượt quá 1000 ký tự")
        String imageUrl
) implements IEventTimeRequest{
}
