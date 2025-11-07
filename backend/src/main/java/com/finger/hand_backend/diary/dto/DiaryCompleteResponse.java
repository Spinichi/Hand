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
     * 완료 시각
     */
    private LocalDateTime completedAt;
}
