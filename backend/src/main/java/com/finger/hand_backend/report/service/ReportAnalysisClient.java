package com.finger.hand_backend.report.service;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${report.api.weekly.url:http://localhost:8000/analyze-week}")
    private String weeklyReportApiUrl;

    @Value("${report.api.monthly.url:http://localhost:8000/analyze-month}")
    private String monthlyReportApiUrl;

    @Value("${report.api.counseling.url:http://localhost:8000/analyze-counseling}")
    private String counselingApiUrl;

    /**
     * 주간 보고서 분석
     *
     * @param diaries     일별 다이어리 리스트 (긴 요약, 우울 점수 포함)
     * @param biometrics  생체 데이터
     * @return 분석 결과
     */
    public ReportAnalysisResult analyzeWeeklyReport(
            List<Map<String, Object>> diaries,
            Map<String, Object> biometrics) {

        log.info("Analyzing weekly report (diaries: {})", diaries.size());

        return callAnalysisApi(weeklyReportApiUrl, diaries, biometrics, "weekly");
    }

    /**
     * 월간 보고서 분석
     *
     * @param diaries     일별 다이어리 리스트 (긴 요약, 우울 점수 포함)
     * @param biometrics  생체 데이터
     * @return 분석 결과
     */
    public ReportAnalysisResult analyzeMonthlyReport(
            List<Map<String, Object>> diaries,
            Map<String, Object> biometrics) {

        log.info("Analyzing monthly report (diaries: {})", diaries.size());

        return callAnalysisApi(monthlyReportApiUrl, diaries, biometrics, "monthly");
    }

    /**
     * FastAPI 호출 공통 메서드
     */
    private ReportAnalysisResult callAnalysisApi(
            String apiUrl,
            List<Map<String, Object>> diaries,
            Map<String, Object> biometrics,
            String reportType) {

        try {
            // 요청 바디 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("diaries", diaries);
            requestBody.put("biometrics", biometrics);

            // FastAPI 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

            log.debug("{} Report API Request: {}", reportType, jsonRequestBody);
            String responseBody = restTemplate.postForObject(apiUrl, request, String.class);
            log.debug("{} Report API Response: {}", reportType, responseBody);

            // 응답 파싱
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode result = root.at("/result");

            return ReportAnalysisResult.builder()
                    .summary(result.get("summary").asText())
                    .emotionalAdvice(result.get("emotional_advice").asText())
                    .trendAnalysis(result.get("trend_analysis").asText())
                    .biometricInsights(result.get("biometric_insights").asText())
                    .build();

        } catch (Exception e) {
            log.warn("{} Report Analysis: FastAPI 호출 실패, Mock 데이터 반환", reportType, e);

            // Fallback: Mock 데이터 반환
            return ReportAnalysisResult.builder()
                    .summary("분석 중 오류가 발생했습니다. 서버 상태를 확인해주세요.")
                    .emotionalAdvice("FastAPI 서버와의 연결에 실패했습니다.")
                    .trendAnalysis("데이터 분석을 완료하지 못했습니다.")
                    .biometricInsights("생체 데이터 분석이 불가능합니다.")
                    .build();
        }
    }

    /**
     * 관리자 상담용 분석
     *
     * @param requestBody 전체 요청 데이터 (userId, period, totalSummary, diaries, biometrics)
     * @return 상담 조언
     */
    public String analyzeCounseling(Map<String, Object> requestBody) {
        log.info("Analyzing counseling");

        try {
            // FastAPI 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

            log.debug("Counseling API Request: {}", jsonRequestBody);
            String responseBody = restTemplate.postForObject(counselingApiUrl, request, String.class);
            log.debug("Counseling API Response: {}", responseBody);

            // 응답 파싱
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode result = root.at("/result");

            return result.get("counselingAdvice").asText();

        } catch (Exception e) {
            log.warn("Counseling Analysis: FastAPI 호출 실패, Mock 데이터 반환", e);

            // Fallback: Mock 데이터 반환
            return "상담 분석 중 오류가 발생했습니다.\n\nFastAPI 서버와의 연결에 실패하여 상담 조언을 생성할 수 없습니다.\n서버 상태를 확인해주세요.";
        }
    }

    /**
     * 보고서 분석 결과
     */
    @lombok.Data
    @lombok.Builder
    public static class ReportAnalysisResult {
        private String summary;           // 주간/월간 요약
        private String emotionalAdvice;   // 감정 개선 조언
        private String trendAnalysis;     // 트렌드 분석
        private String biometricInsights; // 생체 인사이트
    }
}
