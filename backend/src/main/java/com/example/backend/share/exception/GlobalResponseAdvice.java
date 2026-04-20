package com.example.backend.share.exception;

import com.example.backend.dto.response.api.ApiResponse;
import com.example.backend.dto.response.api.abstraction.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {

        if (shouldSkipWrapping(request) || isAlreadyWrapped(body, selectedConverterType)) {
            return body;
        }

        ApiResponse<Object> apiResponse = new ApiResponse<>(resolveStatus(response), true, "Success", body);

        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            return writeStringResponse(apiResponse, response);
        }

        return apiResponse;
    }

    private boolean shouldSkipWrapping(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        return path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html");
    }

    private boolean isAlreadyWrapped(Object body, Class<? extends HttpMessageConverter<?>> converterType) {
        return body instanceof BaseResponse
                || body instanceof byte[]
                || ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    private int resolveStatus(ServerHttpResponse response) {
        if (response instanceof ServletServerHttpResponse servletResponse) {
            return servletResponse.getServletResponse().getStatus();
        }

        return 200;
    }

    private String writeStringResponse(ApiResponse<Object> apiResponse, ServerHttpResponse response) {
        try {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return objectMapper.writeValueAsString(apiResponse);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize ApiResponse", e);
        }
    }
}
