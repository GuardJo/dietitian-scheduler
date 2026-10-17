package org.github.guardjo.dientitian.scheduler.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.github.guardjo.dientitian.scheduler.api.model.BaseResponse;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 전역에서 발생하는 예외를 공통 응답 형식으로 변환하는 핸들러.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * Validation 어노테이션 검증 실패 시 오류 메시지들을 줄바꿈(\n)으로 이어 data에 담아 반환한다.
     * MethodArgumentNotValidException(@RequestBody, @ModelAttribute)은 BindException의 하위 클래스이므로 함께 처리된다.
     * <hr/>
     * HandlerMethodValidationException의 경우 spring framewokr 6.1 부터 Validation check 간 실패 시 반환하는 예외
     * <hr/>
     * HttpMessageNotReadableException의 경우 @JsonFormat 어노테이션이 적용된 팔드의 패턴이 올바르게 입력되지 않을 때 반환하는 예외
     */
    @ExceptionHandler({
            BindException.class,
            HandlerMethodValidationException.class,
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<String> handleValidation(Exception e) {
        log.error("Validation Failed", e);

        String errorMessage = e.getMessage();

        if (e instanceof BindException) {
            errorMessage = ((BindException) e).getBindingResult().getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .collect(Collectors.joining("\n"));
        } else if (e instanceof HandlerMethodValidationException) {
            errorMessage = ((HandlerMethodValidationException) e).getAllErrors().stream()
                    .map(MessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("\n"));
        } else if (e instanceof HttpMessageNotReadableException mne) {
            errorMessage = "요청 형식이 올바르지 않습니다.";

            if (e.getCause() instanceof InvalidFormatException ife) {
                String fieldName = ife.getPath().stream()
                        .map(JacksonException.Reference::getPropertyName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining("."));

                Class<?> targetType = ife.getTargetType();
                if (targetType == LocalDate.class) {
                    errorMessage = "날짜 값은 yyyy-MM-dd 형식이어야 합니다.";
                } else if (targetType == LocalTime.class) {
                    errorMessage = "시각 값은 HH:mm 형식이어야 합니다.";
                }
            }
        }

        return BaseResponse.of(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseResponse<String> handleBadCredentials(Exception e) {
        log.error("Bad Credentials", e);
        return BaseResponse.of(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(ExcelFileReadException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<String> handleInternalServerErrors(Exception e) {
        log.error("Internal Server Error", e);
        return BaseResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
