package com.fourthon.backend.ticket.config;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import java.util.Map;


@RestControllerAdvice(basePackages="com.fourthon.backend.ticket")
public class TicketValidationAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> validation(MethodArgumentNotValidException ex) {
        String message=ex.getBindingResult().getFieldErrors().stream().findFirst()
            .map(e->e.getField()+": "+e.getDefaultMessage()).orElse("입력값이 유효하지 않습니다");
        return Map.of("message",message);
    }
}
