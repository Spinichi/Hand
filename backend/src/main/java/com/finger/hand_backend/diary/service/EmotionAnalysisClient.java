package com.finger.hand_backend.diary.service;

import com.finger.hand_backend.diary.entity.EmotionAnalysis;
import com.finger.hand_backend.diary.entity.QuestionAnswer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 감정 분석 클라이언트
 * - FastAPI 서버로 감정 분석 요청
 * - TODO: FastAPI 서버 구축 후 실제 API 연동 필요
 */
@Service
@Slf4j
public class EmotionAnalysisClient {

    private final Random random = new Random();

    @Value("${emotion.api.url:http://localhost:8000/analyze}")
    private String emotionApiUrl;

    /**
     * 감정 분석
     * TODO: FastAPI 연동 후 실제 구현
     */
    public EmotionAnalysis analyzeEmotion(List<QuestionAnswer> conversationHistory) {
        log.info("EmotionAnalysis: Analyzing (conversation size: {})", conversationHistory.size());

        // TODO: FastAPI 서버로 요청
        // POST {emotionApiUrl}
        // Body: { "conversations": [...] }
        // Response: { "기쁨": 0.05, "당황": 0.12, ..., "우울점수": 65.5 }

        log.warn("EmotionAnalysis: FastAPI 미구현 - Mock 데이터 반환");

        // Mock 데이터 (임시)
        return EmotionAnalysis.builder()
                .joy(randomProbability())
                .embarrassment(randomProbability())
                .anger(randomProbability())
                .anxiety(randomProbability())
                .hurt(randomProbability())
                .sadness(randomProbability())
                .depressionScore(random.nextDouble() * 100) // 0-100
                .analyzedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 0-1 사이 랜덤 확률
     */
    private Double randomProbability() {
        return random.nextDouble();
    }
}
