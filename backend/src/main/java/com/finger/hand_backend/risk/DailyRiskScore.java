package com.finger.hand_backend.risk;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 일일 위험 점수 엔티티
 * - 다이어리 감정 분석 + 측정 데이터 → 종합 위험 점수
 * - 사용자에게는 비노출 (내부 데이터)
 */
@Entity
@Table(
    name = "daily_risk_scores",
    indexes = {
        @Index(name = "idx_user_date", columnList = "user_id, score_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_date", columnNames = {"user_id", "score_date"})
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRiskScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 점수 날짜
     */
    @Column(name = "score_date", nullable = false)
    private LocalDate scoreDate;

    // ========== 종합 점수 ==========

    /**
     * 최종 위험 점수 (0-100)
     * 사용자에게 비노출, 시스템 내부 데이터
     */
    @Column(name = "risk_score")
    private Double riskScore;

    // ========== 각 요소별 기여분 ==========

    /**
     * 다이어리 기여분 (0-100)
     * AI 우울점수
     */
    @Column(name = "diary_component")
    private Double diaryComponent;

    /**
     * 측정 데이터 기여분 (0-100)
     * Measurement.isAnomaly 기반 (이상치 횟수 + 평균 stressIndex)
     */
    @Column(name = "measurement_component")
    private Double measurementComponent;

    /**
     * 수면 기여분 (0-100)
     * 나중에 구현
     */
    @Column(name = "sleep_component")
    private Double sleepComponent;

    // ========== 통계 ==========

    /**
     * 하루 총 측정 횟수
     */
    @Column(name = "measurement_count")
    private Integer measurementCount;

    /**
     * 하루 이상치 감지 횟수
     */
    @Column(name = "anomaly_count")
    private Integer anomalyCount;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 점수 업데이트
     */
    public void updateScores(Double diaryComponent, Double measurementComponent,
                            Double sleepComponent, Double riskScore) {
        this.diaryComponent = diaryComponent;
        this.measurementComponent = measurementComponent;
        this.sleepComponent = sleepComponent;
        this.riskScore = riskScore;
    }

    /**
     * 통계 업데이트
     */
    public void updateStats(Integer measurementCount, Integer anomalyCount) {
        this.measurementCount = measurementCount;
        this.anomalyCount = anomalyCount;
    }
}
