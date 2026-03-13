package com.example.backend.dto.response.api;

import com.example.backend.dto.response.api.abstraction.BaseResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ApiResponse<T> extends BaseResponse {

    private T data;

    public ApiResponse(int code, boolean success, String message, T data) {
        super(code, success, message);
        this.data = data;
    }
}
