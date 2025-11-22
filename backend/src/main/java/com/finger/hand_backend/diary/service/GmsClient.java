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
 * GMS 대화 생성 클라이언트
 * - OpenAI 호환 API 사용
 * - 상담사처럼 질문, 공감, 격려 등 자연스러운 대화 생성 (감정 분석은 별도 FastAPI)
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
     * 다음 대화 생성 (질문, 공감, 격려 등)
     */
    public String generateNextQuestion(List<QuestionAnswer> conversationHistory) {
        log.info("GMS: Generating next response (conversation size: {})", conversationHistory.size());

        // 대화 히스토리를 텍스트로 변환
        String historyText = conversationHistory.stream()
                .map(qa -> String.format("상담사: %s\n사용자: %s",
                        qa.getQuestionText(),
                        qa.getAnswerText()))
                .collect(Collectors.joining("\n\n"));

        // GMS API 요청
        Map<String, Object> request = Map.of(
                "model", "gpt-4.1-nano",
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "당신은 공감 능력이 뛰어난 따뜻한 심리 상담사입니다. " +
                                "사용자의 답변을 듣고 친한 친구처럼 자연스럽게 대화를 이어가세요. " +
                                "\n\n" +
                                "대화 방식:\n" +
                                "1. 질문형: 사용자의 감정이나 상황을 더 깊이 탐색하고 싶을 때 (예: '그때 기분이 어땠어?', '무엇이 가장 힘들었어?')\n" +
                                "2. 공감형: 사용자의 감정을 이해하고 공감할 때 평서문으로 (예: '정말 힘들었겠다.', '그럴 수 있어, 충분히 이해해.')\n" +
                                "3. 격려형: 사용자에게 용기를 주고 싶을 때 평서문으로 (예: '잘 이겨내고 있어.', '넌 충분히 잘하고 있어.')\n" +
                                "\n" +
                                "규칙:\n" +
                                "- 사용자의 상황과 감정에 맞게 위 3가지 방식을 자연스럽게 섞어서 사용하세요.\n" +
                                "- 응답은 30자 이내로 간결하게 작성하세요.\n" +
                                "- 따뜻하고 진심 어린 톤을 유지하세요.\n" +
                                "- 응답만 반환하고 다른 설명은 하지 마세요."),
                        Map.of("role", "user", "content",
                                "지금까지의 대화:\n\n" + historyText + "\n\n위 대화를 바탕으로 다음 응답을 생성하세요.")
                ),
                "max_tokens", 100,
                "temperature", 0.8
        );

        try {
            log.debug("GMS Request Body: {}", objectMapper.writeValueAsString(request));
            JsonNode response = callGmsApi(request);
            String nextResponse = response.at("/choices/0/message/content").asText().trim();
            log.debug("GMS: Generated response: {}", nextResponse);
            return nextResponse;
        } catch (Exception e) {
            log.error("GMS API 호출 실패", e);
            // Fallback: 기본 응답 반환
            return "그 일에 대해 좀 더 이야기해줄 수 있어?";
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
