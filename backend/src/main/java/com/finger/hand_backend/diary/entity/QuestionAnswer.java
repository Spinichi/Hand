package com.finger.hand_backend.diary.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 질문-답변 쌍
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswer {

    /**
     * 질문 번호 (1부터 시작)
     */
    private Integer questionNumber;

    /**
     * 질문 텍스트
     */
    private String questionText;

    /**
     * 질문 출처
     * POOL: 질문 풀에서 선택
     * GMS: AI가 생성
     */
    private QuestionSource source;

    /**
     * 답변 텍스트
     */
    private String answerText;

    /**
     * 답변 시각
     */
    private LocalDateTime answeredAt;
}
