package com.example.backend.dto.response.api;

import com.example.backend.dto.response.api.abstraction.BaseResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PageResponse<T> extends BaseResponse {
    private List<T> data;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public PageResponse(int status,boolean success,String message,List<T> data,
                        int page, int size, long totalElements,int totalPages, boolean last) {
        super(status,success,message);
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }
}
