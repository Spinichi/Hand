package com.finger.hand_backend.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 다이어리 시작 응답
 */
@Getter
@Builder
@AllArgsConstructor
public class DiaryStartResponse {

    /**
     * 세션 ID
     */
    private Long sessionId;

    /**
     * 질문 번호 (1)
     */
    private Integer questionNumber;

    /**
     * 첫 질문 텍스트
     */
    private String questionText;
}
