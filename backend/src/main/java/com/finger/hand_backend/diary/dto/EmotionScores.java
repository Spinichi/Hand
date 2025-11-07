package com.finger.hand_backend.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 감정 점수 (6가지)
 */
@Getter
@Builder
@AllArgsConstructor
public class EmotionScores {

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
}
