package com.finger.hand_backend.diary.dto;

import com.finger.hand_backend.diary.entity.DiaryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 다이어리 목록 응답
 */
@Getter
@Builder
@AllArgsConstructor
public class DiaryListResponse {

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
     * 질문-답변 횟수
     */
    private Integer questionCount;

    /**
     * 생성 시각
     */
    private LocalDateTime createdAt;

    /**
     * 완료 시각
     */
    private LocalDateTime completedAt;

    /**
     * 우울 점수 (완료된 경우에만)
     */
    private Double depressionScore;
}
