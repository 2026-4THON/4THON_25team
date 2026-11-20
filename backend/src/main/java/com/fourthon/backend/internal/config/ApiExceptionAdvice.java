package com.fourthon.backend.internal.config;

import com.fourthon.backend.common.exception.CodedException;
import com.fourthon.backend.internal.dto.ApiErrorResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackages = {
        "com.fourthon.backend.kopis",
        "com.fourthon.backend.show",
        "com.fourthon.backend.map",
        "com.fourthon.backend.taste",
        "com.fourthon.backend.recommendation",
        "com.fourthon.backend.internal"
})
public class ApiExceptionAdvice {

    private static final String INVALID_INPUT = "INVALID_INPUT";
    private static final String INVALID_PARAMETER = "INVALID_PARAMETER";
    private static final String MISSING_PARAMETER = "MISSING_PARAMETER";
    private static final String INVALID_BODY = "INVALID_BODY";
    private static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    private static final String UNKNOWN_STATUS = "ERROR";
    private static final String DEFAULT_INVALID_MESSAGE = "입력값이 유효하지 않습니다";
    private static final String DEFAULT_ERROR_MESSAGE = "요청을 처리할 수 없습니다";
    private static final String INVALID_BODY_MESSAGE = "요청 본문을 읽을 수 없습니다. JSON 형식과 값의 종류를 확인해 주세요";
    private static final String INTERNAL_ERROR_MESSAGE = "요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요";
    private static final String DELIMITER = ", ";
    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.of(
            "NotNull", "값이 필요합니다",
            "NotBlank", "비어 있을 수 없습니다",
            "NotEmpty", "하나 이상 있어야 합니다",
            "Size", "길이나 개수가 허용 범위를 벗어났습니다",
            "Min", "허용된 최솟값보다 작습니다",
            "Max", "허용된 최댓값보다 큽니다",
            "Positive", "0보다 커야 합니다",
            "PositiveOrZero", "0 이상이어야 합니다",
            "Pattern", "형식이 올바르지 않습니다"
    );

    @ExceptionHandler(CodedException.class)
    public ResponseEntity<ApiErrorResponse> handleCoded(CodedException exception) {
        return respond(exception.getStatus(), exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidBody(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(this::describe)
                .collect(Collectors.joining(DELIMITER));
        return respond(HttpStatus.BAD_REQUEST, INVALID_INPUT, message.isEmpty() ? DEFAULT_INVALID_MESSAGE : message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String message = exception.getName() + " 값이 올바르지 않습니다: " + exception.getValue();
        Class<?> requiredType = exception.getRequiredType();
        if (requiredType != null && requiredType.isEnum()) {
            message += " (가능한 값: " + Arrays.stream(requiredType.getEnumConstants())
                    .map(String::valueOf)
                    .collect(Collectors.joining(DELIMITER)) + ")";
        }
        return respond(HttpStatus.BAD_REQUEST, INVALID_PARAMETER, message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException exception) {
        return respond(HttpStatus.BAD_REQUEST, MISSING_PARAMETER, "필수 값이 없습니다: " + exception.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return respond(HttpStatus.BAD_REQUEST, INVALID_BODY, INVALID_BODY_MESSAGE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        if (exception instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            String detail = errorResponse.getBody().getDetail();
            return respond(status, codeOf(status), detail == null || detail.isBlank() ? DEFAULT_ERROR_MESSAGE : detail);
        }
        log.error("처리하지 못한 예외", exception);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_ERROR, INTERNAL_ERROR_MESSAGE);
    }

    private String describe(FieldError error) {
        String message = CONSTRAINT_MESSAGES.getOrDefault(error.getCode(), error.getDefaultMessage());
        return error.getField() + ": " + (message == null ? DEFAULT_INVALID_MESSAGE : message);
    }

    private String codeOf(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved == null ? UNKNOWN_STATUS : resolved.name();
    }

    private ResponseEntity<ApiErrorResponse> respond(HttpStatusCode status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(code, message));
    }
}
