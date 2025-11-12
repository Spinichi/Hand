package com.finger.hand_backend.diary.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finger.hand_backend.diary.entity.EmotionAnalysis;
import com.finger.hand_backend.diary.entity.QuestionAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 감정 분석 클라이언트
 * - FastAPI 서버로 감정 분석 요청
 * - TODO: FastAPI 서버 구축 후 실제 API 연동 필요
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmotionAnalysisClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @Value("${emotion.api.url:http://localhost:8000/analyze}")
    private String emotionApiUrl;

    /**
     * 감정 분석
     * FastAPI 서버로 대화 내용 전송 후 감정 분석 결과 수신
     */
    public EmotionAnalysis analyzeEmotion(List<QuestionAnswer> conversationHistory) {
        log.info("EmotionAnalysis: Analyzing (conversation size: {})", conversationHistory.size());

        try {
            // 답변들만 이어붙여서 하나의 문자열로 생성
            String diary = conversationHistory.stream()
                    .filter(qa -> qa.getAnswerText() != null) // 답변이 있는 것만
                    .map(QuestionAnswer::getAnswerText)
                    .collect(Collectors.joining(" "));

            Map<String, Object> requestBody = Map.of("diary", diary);

            // FastAPI 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

            log.debug("Emotion API Request: {}", jsonRequestBody);
            String responseBody = restTemplate.postForObject(emotionApiUrl, request, String.class);
            log.debug("Emotion API Response: {}", responseBody);

            // 응답 파싱
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode result = root.at("/result");
            JsonNode sentiment = result.at("/sentiment");

            return EmotionAnalysis.builder()
                    .joy(sentiment.get("기쁨").asDouble())
                    .embarrassment(sentiment.get("당황").asDouble())
                    .anger(sentiment.get("분노").asDouble())
                    .anxiety(sentiment.get("불안").asDouble())
                    .hurt(sentiment.get("상처").asDouble())
                    .sadness(sentiment.get("슬픔").asDouble())
                    .depressionScore(result.get("score").asDouble())
                    .shortSummary(result.get("short_summary").asText())
                    .longSummary(result.get("long_summary").asText())
                    .analyzedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.warn("EmotionAnalysis: FastAPI 호출 실패, Mock 데이터 반환", e);

            // Fallback: Mock 데이터 반환
            return EmotionAnalysis.builder()
                    .joy(randomProbability())
                    .embarrassment(randomProbability())
                    .anger(randomProbability())
                    .anxiety(randomProbability())
                    .hurt(randomProbability())
                    .sadness(randomProbability())
                    .depressionScore(random.nextDouble() * 100)
                    .shortSummary("감정 분석 중 오류가 발생했습니다.")
                    .longSummary("FastAPI 서버와의 연결에 실패하여 임시 데이터를 반환합니다. 서버 상태를 확인해주세요.")
                    .analyzedAt(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 0-1 사이 랜덤 확률
     */
    private Double randomProbability() {
        return random.nextDouble();
    }
}
