package com.finger.hand_backend.diary.service;

import com.finger.hand_backend.diary.entity.QuestionAnswer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GMS 질문 생성 클라이언트
 * - OpenAI 호환 API 사용
 * - 다음 질문만 생성 (감정 분석은 별도 FastAPI)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GmsClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gms.api.url:https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions}")
    private String gmsApiUrl;

    @Value("${gms.api.key}")
    private String gmsApiKey;

    /**
     * 다음 질문 생성
     */
    public String generateNextQuestion(List<QuestionAnswer> conversationHistory) {
        log.info("GMS: Generating next question (conversation size: {})", conversationHistory.size());

        // 대화 히스토리를 텍스트로 변환
        String historyText = conversationHistory.stream()
                .map(qa -> String.format("Q%d: %s\nA%d: %s",
                        qa.getQuestionNumber(), qa.getQuestionText(),
                        qa.getQuestionNumber(), qa.getAnswerText()))
                .collect(Collectors.joining("\n\n"));

        // GMS API 요청
        Map<String, Object> request = Map.of(
                "model", "gpt-4.1-nano",
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "당신은 공감 능력이 뛰어난 심리 상담사입니다. " +
                                "사용자의 답변을 보고 당신이 이사람의 친한 친구하고 생각하고 후속 질문을 하나만 생성하세요. " +
                                "질문은 따뜻하고 공감적이며, 대화처럼 30자 이내로 간결해야 합니다. " +
                                "질문만 반환하고 다른 설명은 하지 마세요."),
                        Map.of("role", "user", "content",
                                "지금까지의 대화:\n\n" + historyText + "\n\n위 대화를 바탕으로 다음 질문을 생성하세요.")
                ),
                "max_tokens", 100,
                "temperature", 0.7
        );

        try {
            log.debug("GMS Request Body: {}", objectMapper.writeValueAsString(request));
            JsonNode response = callGmsApi(request);
            String question = response.at("/choices/0/message/content").asText().trim();
            log.debug("GMS: Generated question: {}", question);
            return question;
        } catch (Exception e) {
            log.error("GMS API 호출 실패", e);
            // Fallback: 기본 질문 반환
            return "그 일에 대해 좀 더 자세히 말씀해주시겠어요?";
        }
    }

    /**
     * GMS API 호출
     */
    private JsonNode callGmsApi(Map<String, Object> requestBody) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gmsApiKey);

        log.debug("GMS API URL: {}", gmsApiUrl);
        log.debug("GMS API Key: {}...", gmsApiKey.substring(0, Math.min(20, gmsApiKey.length())));

        // Map을 JSON 문자열로 명시적으로 변환
        String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

        String responseBody = restTemplate.postForObject(gmsApiUrl, request, String.class);
        log.debug("GMS Response: {}", responseBody);
        return objectMapper.readTree(responseBody);
    }
}
