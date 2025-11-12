package com.finger.hand_backend.measurement;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 측정 데이터 엔티티
 * - 워치가 보낸 측정 데이터 저장
 * - HRV, 스트레스, 이상치 탐지 등 모두 워치에서 계산되어 전송됨
 */
@Entity
@Table(
    name = "measurements",
    indexes = {
        @Index(name = "idx_user_measured", columnList = "user_id, measured_at")
    }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ========== 워치가 보낸 raw data ==========

    /**
     * 심박수 (bpm)
     * 워치에서 측정된 심박수 (소수점 포함)
     */
    @Column(name = "heart_rate", nullable = false)
    private Double heartRate;

    /**
     * 피부 온도 (°C)
     */
    @Column(name = "object_temp")
    private Double objectTemp;

    /**
     * 주변 온도 (°C)
     */
    @Column(name = "ambient_temp")
    private Double ambientTemp;

    /**
     * 가속도계 X축 (소수점 포함)
     */
    @Column(name = "accel_x")
    private Double accelX;

    /**
     * 가속도계 Y축 (소수점 포함)
     */
    @Column(name = "accel_y")
    private Double accelY;

    /**
     * 가속도계 Z축 (소수점 포함)
     */
    @Column(name = "accel_z")
    private Double accelZ;

    // ========== 워치에서 계산된 값 ==========

    /**
     * HRV SDNN (ms)
     * 워치에서 IBI 배열의 표준편차 계산
     */
    @Column(name = "hrv_sdnn")
    private Double hrvSdnn;

    /**
     * HRV RMSSD (ms)
     * 워치에서 IBI 연속 차이의 제곱평균제곱근 계산
     */
    @Column(name = "hrv_rmssd")
    private Double hrvRmssd;

    /**
     * 움직임 강도
     * 워치에서 전송 (Movement 값)
     */
    @Column(name = "movement_intensity")
    private Double movementIntensity;

    /**
     * 스트레스 지수 (0-100)
     * 워치에서 계산된 스트레스 수치 (소수점 포함)
     * 예: 51.1
     */
    @Column(name = "stress_index")
    private Double stressIndex;

    /**
     * 스트레스 단계 (1-5)
     * 워치에서 계산된 스트레스 레벨
     * 1: 매우 편안, 2: 편안, 3: 보통, 4: 스트레스, 5: 고스트레스
     */
    @Column(name = "stress_level")
    private Integer stressLevel;

    /**
     * 이상치 여부
     * 워치에서 이상치 탐지 수행
     */
    @Column(name = "is_anomaly", nullable = false)
    private Boolean isAnomaly;

    // ========== 활동 감지 ==========

    /**
     * 누적 걸음수 (Steps)
     */
    @Column(name = "total_steps")
    private Integer totalSteps;

    /**
     * 분당 걸음수 (SPM)
     * 워치에서 계산된 값
     */
    @Column(name = "steps_per_minute")
    private Double stepsPerMinute;

    // ========== 시간 ==========

    /**
     * 측정 시각 (워치에서 측정한 시간)
     */
    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    /**
     * 생성 시각 (서버에 저장된 시간)
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
