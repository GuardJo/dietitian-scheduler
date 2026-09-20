package org.github.guardjo.dientitian.scheduler.api.model;

import org.springframework.http.HttpStatus;

public record BaseResponse<T>(
        int status,
        String statusText,
        T data
) {
    public static <D> BaseResponse<D> of(HttpStatus status, D data) {
        return new BaseResponse<>(status.value(), status.name(), data);
    }
}
