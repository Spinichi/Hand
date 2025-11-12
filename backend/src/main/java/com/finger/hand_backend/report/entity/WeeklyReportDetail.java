package com.finger.hand_backend.report.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 주간보고서 상세 (MongoDB)
 * - 모든 보고서 데이터 포함
 */
@Document(collection = "weekly_report_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReportDetail {

    @Id
    private String id;

    private Long userId;
    private Integer year;
    private Integer weekNumber;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;

    // ========== 다이어리 데이터 ==========

    /**
     * 일별 다이어리 (7개: 월~일)
     * [{ date, longSummary, shortSummary, depressionScore }, ...]
     */
    private List<Map<String, Object>> dailyDiaries;

    // ========== 생체 데이터 ==========

    /**
     * 사용자 베이스라인
     */
    private Map<String, Object> userBaseline;

    /**
     * 이상치 상세 정보
     * [{ detectedAt, measurementId, stressIndex, stressLevel, heartRate, hrvSdnn, hrvRmssd }, ...]
     */
    private List<Map<String, Object>> anomalies;

    /**
     * 사용자 기본 정보 (AI 분석 참고용)
     */
    private Map<String, Object> userInfo;

    // ========== AI 분석 결과 ==========

    /**
     * 주간 보고서 (통합)
     * - 이전의 weeklySummary, trendAnalysis, biometricInsights 통합
     */
    private String report;

    /**
     * 감정 조언
     */
    private String emotionalAdvice;

    // ========== 통계 ==========

    private Integer totalDiaryCount;
    private Double averageDepressionScore;
    private Double maxDepressionScore;
    private Double minDepressionScore;

    // ========== 메타 ==========

    private LocalDateTime createdAt;
    private LocalDateTime analyzedAt;
}
