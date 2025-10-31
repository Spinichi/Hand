package com.finger.hand_backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", "VALIDATION_ERROR"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegal(IllegalArgumentException ex) {
        String code = ex.getMessage() == null ? "BAD_REQUEST" : ex.getMessage();
        HttpStatus status = "INVALID_CREDENTIALS".equals(code) ? HttpStatus.UNAUTHORIZED :
                "EMAIL_ALREADY_EXISTS".equals(code) ? HttpStatus.CONFLICT :
                        HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of("message", code));
    }
}
