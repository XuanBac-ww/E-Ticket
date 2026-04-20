package com.example.backend.dto.response.api;

import com.example.backend.dto.response.api.abstraction.BaseResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> extends BaseResponse {
    @Nullable
    private T data;

    public ApiResponse(int code, boolean success, String message, @Nullable T data) {
        super(code, success, message);
        this.data = data;
    }
}
