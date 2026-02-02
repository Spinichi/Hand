package com.finger.hand_backend.common;

import com.finger.hand_backend.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 핸들러
 * - 모든 예외를 일관된 형식으로 처리
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * IllegalArgumentException 처리
     * - 비즈니스 로직 검증 실패 시 발생
     * - 특정 메시지는 401, 409로 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String message = ex.getMessage();

        // 인증 실패 → 401
        if ("INVALID_CREDENTIALS".equals(message)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 틀렸습니다"));
        }

        // 이메일 중복 → 409
        if ("EMAIL_ALREADY_EXISTS".equals(message)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다"));
        }

        // 리소스 없음 → 404
        if (message != null && message.contains("NOT_FOUND")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("NOT_FOUND", message));
        }

        // 기본 400
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("BAD_REQUEST", message != null ? message : "잘못된 요청입니다"));
    }

    /**
     * IllegalStateException 처리
     * - 시스템 상태 오류 (예: 질문 풀 비어있음)
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.error("IllegalStateException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", ex.getMessage()));
    }

    /**
     * 낙관적 락 충돌 처리
     * - 동시 요청으로 인한 version 충돌
     * - 이미 다른 요청이 완료 처리함 → 409 Conflict
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("🔒 낙관적 락 충돌: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("ALREADY_COMPLETED", "이미 완료된 다이어리입니다"));
    }

    /**
     * Validation 에러 처리
     * - @Valid 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    /**
     * 예상치 못한 모든 예외 처리
     * - 최후의 안전망
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다"));
    }
}
