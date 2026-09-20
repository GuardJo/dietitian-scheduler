package org.github.guardjo.dientitian.scheduler.api.exception;

import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역에서 발생하는 예외를 공통 응답 형식으로 변환하는 핸들러.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseResponse<String> handleBadCredentials(Exception e) {
        return BaseResponse.of(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
}
