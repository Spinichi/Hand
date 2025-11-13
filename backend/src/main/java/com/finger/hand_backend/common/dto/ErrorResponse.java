package com.finger.hand_backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 에러 응답 DTO
 * - 모든 API 에러에 대한 통일된 응답 형식
 */
@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    /**
     * 에러 코드
     * 예: "INVALID_CREDENTIALS", "QUESTION_POOL_EMPTY", "VALIDATION_ERROR"
     */
    private final String code;

    /**
     * 에러 메시지
     * 예: "이메일 또는 비밀번호가 틀렸습니다"
     */
    private final String message;
}
