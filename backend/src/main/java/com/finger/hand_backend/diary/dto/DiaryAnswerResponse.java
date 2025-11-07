package com.finger.hand_backend.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 답변 제출 후 다음 질문 응답
 */
@Getter
@Builder
@AllArgsConstructor
public class DiaryAnswerResponse {

    /**
     * 세션 ID
     */
    private Long sessionId;

    /**
     * 다음 질문 번호
     */
    private Integer questionNumber;

    /**
     * 다음 질문 텍스트
     */
    private String questionText;

    /**
     * 종료 가능 여부 (최소 3개 이상)
     */
    private Boolean canFinish;
}
