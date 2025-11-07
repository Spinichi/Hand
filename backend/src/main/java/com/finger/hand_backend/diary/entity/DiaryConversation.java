package com.finger.hand_backend.diary.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 다이어리 대화 내용 (MongoDB)
 * - 실제 Q&A 대화 내용 저장
 * - AI 감정 분석 결과 저장
 */
@Document(collection = "diary_conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaryConversation {

    @Id
    private String id; // MongoDB ObjectId

    private Long userId;

    private LocalDate sessionDate;

    /**
     * 질문-답변 목록
     */
    @Builder.Default
    private List<QuestionAnswer> questions = new ArrayList<>();

    /**
     * AI 감정 분석 결과
     */
    private EmotionAnalysis emotionAnalysis;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 질문-답변 추가
     */
    public void addQuestion(QuestionAnswer qa) {
        this.questions.add(qa);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 감정 분석 결과 저장
     */
    public void setEmotionAnalysisResult(EmotionAnalysis analysis) {
        this.emotionAnalysis = analysis;
        this.updatedAt = LocalDateTime.now();
    }
}
