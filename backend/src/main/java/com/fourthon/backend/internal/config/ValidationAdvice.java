package com.fourthon.backend.internal.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = {
        "com.fourthon.backend.kopis",
        "com.fourthon.backend.show",
        "com.fourthon.backend.map",
        "com.fourthon.backend.taste",
        "com.fourthon.backend.recommendation",
        "com.fourthon.backend.internal"
})
public class ValidationAdvice {

    private static final String DEFAULT_MESSAGE = "입력값이 유효하지 않습니다";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse(DEFAULT_MESSAGE);
        return Map.of("message", message);
    }
}
