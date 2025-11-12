package com.finger.hand_backend.diary.dto;

import com.finger.hand_backend.diary.entity.DiaryStatus;
import com.finger.hand_backend.diary.entity.QuestionAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 다이어리 상세 응답
 * - 전체 대화 내용 포함
 */
@Getter
@Builder
@AllArgsConstructor
public class DiaryDetailResponse {

    /**
     * 세션 ID
     */
    private Long sessionId;

    /**
     * 세션 날짜
     */
    private LocalDate sessionDate;

    /**
     * 상태
     */
    private DiaryStatus status;

    /**
     * 질문-답변 목록
     */
    private List<QuestionAnswer> conversations;

    /**
     * 6가지 감정 점수 (완료된 경우에만)
     */
    private EmotionScores emotions;

    /**
     * 우울 점수 (완료된 경우에만)
     */
    private Double depressionScore;

    /**
     * 짧은 요약 (완료된 경우에만)
     */
    private String shortSummary;

    /**
     * 긴 요약 (완료된 경우에만)
     */
    private String longSummary;

    /**
     * 감정 조언 (완료된 경우에만)
     */
    private String emotionalAdvice;

    /**
     * 생성 시각
     */
    private LocalDateTime createdAt;

    /**
     * 완료 시각
     */
    private LocalDateTime completedAt;
}
