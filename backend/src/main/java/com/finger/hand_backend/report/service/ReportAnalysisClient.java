package com.finger.hand_backend.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 보고서 분석 클라이언트
 * - FastAPI 서버로 주간/월간 보고서 분석 요청
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportAnalysisClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());  // LocalDate 직렬화 지원

    @Value("${report.api.individual.url}")
    private String individualReportApiUrl;

    @Value("${report.api.manager.url}")
    private String managerAdviceApiUrl;

    /**
     * 개인 사용자 보고서 분석
     *
     * FastAPI 엔드포인트: POST /ai/individual-users/report
     *
     * @param userId        사용자 ID
     * @param diaries       일별 다이어리 리스트 (date, longSummary, shortSummary, depressionScore)
     * @param biometrics    생체 데이터 (baseline, anomalies, userInfo)
     * @param totalSummary  개인은 "" 값
     * @return 분석 결과 (report, advice)
     */
    public ReportAnalysisResult analyzeIndividualReport(
            Long userId,
            List<Map<String, Object>> diaries,
            Map<String, Object> biometrics,
            String totalSummary) {

        log.info("Analyzing individual report for user {} (diaries: {})", userId, diaries.size());

        return callAnalysisApi(individualReportApiUrl, userId, diaries, biometrics, totalSummary, "individual");
    }

    /**
     * 관리자(팀장)용 보고서 및 조언 생성
     *
     * FastAPI 엔드포인트: POST /ai/manager/advice
     *
     * @param userId        관리자 ID
     * @param diaries       팀원의 일별 다이어리 리스트 (date, longSummary, shortSummary, depressionScore)
     * @param biometrics    팀원의 생체 데이터 (baseline, anomalies, userInfo)
     * @param totalSummary  팀 전체 요약
     * @return 분석 결과 (report, advice)
     */
    public ReportAnalysisResult analyzeManagerAdvice(
            Long userId,
            List<Map<String, Object>> diaries,
            Map<String, Object> biometrics,
            String totalSummary) {

        log.info("Analyzing manager advice for user {} (diaries: {})", userId, diaries.size());

        return callAnalysisApi(managerAdviceApiUrl, userId, diaries, biometrics, totalSummary, "manager");
    }

    /**
     * FastAPI 호출 공통 메서드
     *
     * API 스펙:
     * - Request: { "user_id": int, "diaries": [...], "biometrics": {...}, "total_summary": string }
     * - Response: { "user_id": int, "report": string, "advice": string }
     */
    private ReportAnalysisResult callAnalysisApi(
            String apiUrl,
            Long userId,
            List<Map<String, Object>> diaries,
            Map<String, Object> biometrics,
            String totalSummary,
            String reportType) {

        try {
            // FastAPI 스펙에 맞춘 요청 바디
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("user_id", userId);           // userId -> user_id
            requestBody.put("diaries", diaries);          // 그대로 전달
            requestBody.put("biometrics", biometrics);    // 그대로 전달
            requestBody.put("total_summary", totalSummary); // 추가됨

            // FastAPI 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

            log.debug("{} Report API Request: {}", reportType, jsonRequestBody);
            String responseBody = restTemplate.postForObject(apiUrl, request, String.class);
            log.debug("{} Report API Response: {}", reportType, responseBody);

            // 응답 파싱 (FastAPI 스펙)
            JsonNode root = objectMapper.readTree(responseBody);

            return ReportAnalysisResult.builder()
                    .report(root.get("report").asText())
                    .advice(root.get("advice").asText())  // emotional_advice -> advice
                    .build();

        } catch (Exception e) {
            log.warn("{} Report Analysis: FastAPI 호출 실패, Mock 데이터 반환", reportType, e);

            // Fallback: Mock 데이터 반환
            return ReportAnalysisResult.builder()
                    .report("분석 중 오류가 발생했습니다. 서버 상태를 확인해주세요.")
                    .advice("FastAPI 서버와의 연결에 실패했습니다.")
                    .build();
        }
    }

    /**
     * 보고서 분석 결과 (FastAPI 응답 스펙)
     */
    @lombok.Data
    @lombok.Builder
    public static class ReportAnalysisResult {
        private String report;   // 보고서 내용
        private String advice;   // 조언 (emotional_advice -> advice로 변경됨)
    }
}
