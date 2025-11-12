package com.finger.hand_backend.diary.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI 감정 분석 결과
 * - 6가지 감정 (0-1 확률)
 * - 우울 점수 (0-100)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionAnalysis {

    /**
     * 기쁨 (0-1)
     */
    private Double joy;

    /**
     * 당황 (0-1)
     */
    private Double embarrassment;

    /**
     * 분노 (0-1)
     */
    private Double anger;

    /**
     * 불안 (0-1)
     */
    private Double anxiety;

    /**
     * 상처 (0-1)
     */
    private Double hurt;

    /**
     * 슬픔 (0-1)
     */
    private Double sadness;

    /**
     * 우울 점수 (0-100)
     * daily_risk_scores의 diary_component로 사용됨
     */
    private Double depressionScore;

    /**
     * 짧은 요약
     * 예: "모든 일이 버거운 날."
     */
    private String shortSummary;

    /**
     * 긴 요약
     * 예: "퇴근 후 모든 일이 버겁게 느껴지고, 자신에 대한 확신이 없어..."
     */
    private String longSummary;

    /**
     * 감정 조언
     * 예: "긴장 완화를 위해 심호흡을 시도해보세요. 충분한 휴식도 중요합니다."
     */
    private String emotionalAdvice;

    /**
     * 분석 시각
     */
    private LocalDateTime analyzedAt;
}
