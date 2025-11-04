package com.finger.hand_backend.baseline;

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
 * Baseline 엔티티
 * - 사용자별 개인화된 스트레스 기준값
 * - 편안한 상태(stress_level ≤ 2) 측정 데이터로부터 계산
 * - 버전 관리 및 이력 보관
 */
@Entity
@Table(
    name = "user_baselines",
    indexes = {
        @Index(name = "idx_user_active", columnList = "user_id, is_active"),
        @Index(name = "idx_user_version", columnList = "user_id, version")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_version", columnNames = {"user_id", "version"})
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Baseline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 버전 번호 (1부터 시작)
     * 업데이트시 증가
     */
    @Column(nullable = false)
    private Integer version;

    /**
     * 활성 상태
     * 사용자당 하나만 true
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // ========== HRV SDNN 통계 ==========

    /**
     * HRV SDNN 평균값 (ms)
     */
    @Column(name = "hrv_sdnn_mean")
    private Double hrvSdnnMean;

    /**
     * HRV SDNN 표준편차 (ms)
     */
    @Column(name = "hrv_sdnn_std")
    private Double hrvSdnnStd;

    // ========== HRV RMSSD 통계 ==========

    /**
     * HRV RMSSD 평균값 (ms)
     */
    @Column(name = "hrv_rmssd_mean")
    private Double hrvRmssdMean;

    /**
     * HRV RMSSD 표준편차 (ms)
     */
    @Column(name = "hrv_rmssd_std")
    private Double hrvRmssdStd;

    // ========== 심박수 통계 ==========

    /**
     * 심박수 평균값 (bpm)
     */
    @Column(name = "heart_rate_mean")
    private Double heartRateMean;

    /**
     * 심박수 표준편차 (bpm)
     */
    @Column(name = "heart_rate_std")
    private Double heartRateStd;

    // ========== 체온 통계 ==========

    /**
     * 피부 온도 평균값 (°C)
     */
    @Column(name = "object_temp_mean")
    private Double objectTempMean;

    /**
     * 피부 온도 표준편차 (°C)
     */
    @Column(name = "object_temp_std")
    private Double objectTempStd;

    // ========== 스트레스 임계값 ==========

    /**
     * 낮은 스트레스 임계값 (1-2단계 경계)
     * 이 값 미만: 매우 편안/편안
     */
    @Column(name = "stress_threshold_low")
    private Integer stressThresholdLow;

    /**
     * 중간 스트레스 임계값 (2-3단계 경계)
     * 이 값 미만: 보통
     */
    @Column(name = "stress_threshold_medium")
    private Integer stressThresholdMedium;

    /**
     * 높은 스트레스 임계값 (3-4단계 경계)
     * 이 값 이상: 스트레스/고스트레스
     */
    @Column(name = "stress_threshold_high")
    private Integer stressThresholdHigh;

    // ========== 메타데이터 ==========

    /**
     * 계산에 사용된 측정 데이터 개수
     */
    @Column(name = "measurement_count")
    private Integer measurementCount;

    /**
     * 측정 데이터 시작 날짜
     */
    @Column(name = "data_start_date")
    private LocalDate dataStartDate;

    /**
     * 측정 데이터 종료 날짜
     */
    @Column(name = "data_end_date")
    private LocalDate dataEndDate;

    // ========== 시간 ==========

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
     * 활성화/비활성화
     */
    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
