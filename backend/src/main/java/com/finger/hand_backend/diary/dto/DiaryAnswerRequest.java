package com.finger.hand_backend.diary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 답변 제출 요청
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryAnswerRequest {

    /**
     * 답변 텍스트
     */
    @NotBlank(message = "답변을 입력해주세요")
    private String answerText;
}
