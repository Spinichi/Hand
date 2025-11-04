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
 * - 워치가 보낸 raw data 저장
 * - 백엔드에서 HRV, 스트레스 지수 계산 후 저장
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
     */
    @Column(name = "heart_rate", nullable = false)
    private Integer heartRate;

    /**
     * IBI 배열 (Inter-Beat Interval)
     * JSON 문자열로 저장: "[495, 588, 510, 520, ...]"
     */
    @Column(name = "ibi_array", columnDefinition = "JSON")
    private String ibiArray;

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
     * 가속도계 X축
     */
    @Column(name = "accel_x")
    private Integer accelX;

    /**
     * 가속도계 Y축
     */
    @Column(name = "accel_y")
    private Integer accelY;

    /**
     * 가속도계 Z축
     */
    @Column(name = "accel_z")
    private Integer accelZ;

    // ========== 백엔드에서 계산한 값 ==========

    /**
     * HRV SDNN (ms)
     * IBI 배열의 표준편차
     */
    @Column(name = "hrv_sdnn")
    private Double hrvSdnn;

    /**
     * HRV RMSSD (ms)
     * IBI 연속 차이의 제곱평균제곱근
     */
    @Column(name = "hrv_rmssd")
    private Double hrvRmssd;

    /**
     * 움직임 강도
     * √(x² + y² + z²)
     */
    @Column(name = "movement_intensity")
    private Double movementIntensity;

    /**
     * 스트레스 지수 (1-100)
     */
    @Column(name = "stress_index")
    private Integer stressIndex;

    /**
     * 스트레스 단계 (1-5)
     * 1: 매우 편안, 2: 편안, 3: 보통, 4: 스트레스, 5: 고스트레스
     */
    @Column(name = "stress_level")
    private Integer stressLevel;

    // ========== 활동 감지 ==========

    /**
     * 누적 걸음수 (부팅 후)
     */
    @Column(name = "total_steps")
    private Integer totalSteps;

    /**
     * 가장 최근 걸음 시간
     */
    @Column(name = "last_step_at")
    private LocalDateTime lastStepAt;

    /**
     * 분당 걸음수
     */
    @Column(name = "steps_per_minute")
    private Double stepsPerMinute;

    /**
     * 활동 상태
     * STATIC: 정지 (신뢰도 높음)
     * WALKING: 보행 중 (신뢰도 낮음)
     */
    @Column(name = "activity_state", length = 20)
    private String activityState;

    /**
     * 측정 품질
     * HIGH: 높음 (STATIC 상태)
     * LOW: 낮음 (WALKING 상태)
     */
    @Column(name = "measurement_quality", length = 10)
    private String measurementQuality;

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

    /**
     * 엔티티 저장 전 자동 계산
     * - 움직임 강도 (가속도계 데이터 있는 경우)
     */
    @PrePersist
    protected void onCreate() {
        // 움직임 강도 계산
        if (accelX != null && accelY != null && accelZ != null) {
            this.movementIntensity = Math.sqrt(
                Math.pow(accelX, 2) +
                Math.pow(accelY, 2) +
                Math.pow(accelZ, 2)
            );
        }
    }
}
