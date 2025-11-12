package com.finger.hand_backend.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 다이어리 완료 응답
 * - 사용자에게는 감정 분석 결과만 노출
 * - risk_score는 내부 데이터로만 저장
 */
@Getter
@Builder
@AllArgsConstructor
public class DiaryCompleteResponse {

    /**
     * 세션 ID
     */
    private Long sessionId;

    /**
     * 6가지 감정 점수
     */
    private EmotionScores emotions;

    /**
     * 우울 점수 (0-100)
     */
    private Double depressionScore;

    /**
     * 짧은 요약
     * 예: "모든 일이 버거운 날."
     */
    private String shortSummary;

    /**
     * 긴 요약
     * 예: "퇴근 후 모든 일이 버겁게 느껴지고..."
     */
    private String longSummary;

    /**
     * 감정 조언
     * 예: "긴장 완화를 위해 심호흡을 시도해보세요."
     */
    private String emotionalAdvice;

    /**
     * 완료 시각
     */
    private LocalDateTime completedAt;
}
