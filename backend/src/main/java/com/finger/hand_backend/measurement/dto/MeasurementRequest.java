package com.finger.hand_backend.measurement.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 측정 데이터 저장 요청 DTO
 * - 워치가 백엔드로 전송하는 raw data
 */
@Getter
@Setter
@NoArgsConstructor
public class MeasurementRequest {

    /**
     * 심박수 (bpm)
     * 정상 범위: 30-200
     * 워치에서 측정된 심박수 (소수점 포함)
     */
    @NotNull(message = "심박수는 필수입니다")
    @DecimalMin(value = "30.0", message = "심박수는 30 이상이어야 합니다")
    @DecimalMax(value = "200.0", message = "심박수는 200 이하여야 합니다")
    private Double heartRate;

    /**
     * 피부 온도 (°C)
     * 정상 범위: 20-45
     */
    @DecimalMin(value = "20.0", message = "피부 온도는 20°C 이상이어야 합니다")
    @DecimalMax(value = "45.0", message = "피부 온도는 45°C 이하여야 합니다")
    private Double objectTemp;

    /**
     * 주변 온도 (°C)
     */
    @DecimalMin(value = "10.0", message = "주변 온도는 10°C 이상이어야 합니다")
    @DecimalMax(value = "50.0", message = "주변 온도는 50°C 이하여야 합니다")
    private Double ambientTemp;

    /**
     * 가속도계 X축 (소수점 포함)
     */
    private Double accelX;

    /**
     * 가속도계 Y축 (소수점 포함)
     */
    private Double accelY;

    /**
     * 가속도계 Z축 (소수점 포함)
     */
    private Double accelZ;

    /**
     * HRV SDNN (ms)
     * 워치에서 계산된 값
     */
    @DecimalMin(value = "0.0", message = "HRV SDNN은 0 이상이어야 합니다")
    private Double hrvSdnn;

    /**
     * HRV RMSSD (ms)
     * 워치에서 계산된 값
     */
    @DecimalMin(value = "0.0", message = "HRV RMSSD는 0 이상이어야 합니다")
    private Double hrvRmssd;

    /**
     * 움직임 강도
     * 워치에서 전송 (Movement 값)
     */
    @DecimalMin(value = "0.0", message = "움직임 강도는 0 이상이어야 합니다")
    private Double movementIntensity;

    /**
     * 스트레스 지수 (0-100)
     * 워치에서 계산된 스트레스 수치
     */
    @DecimalMin(value = "0.0", message = "스트레스 지수는 0 이상이어야 합니다")
    @DecimalMax(value = "100.0", message = "스트레스 지수는 100 이하여야 합니다")
    private Double stressIndex;

    /**
     * 스트레스 레벨 (1-5)
     * 워치에서 계산된 스트레스 단계
     */
    @Min(value = 1, message = "스트레스 레벨은 1 이상이어야 합니다")
    @Max(value = 5, message = "스트레스 레벨은 5 이하여야 합니다")
    private Integer stressLevel;

    /**
     * 이상치 여부
     * 워치에서 이상치 탐지 수행
     */
    @NotNull(message = "이상치 여부는 필수입니다")
    private Boolean isAnomaly;

    /**
     * 누적 걸음수 (Steps)
     */
    @Min(value = 0, message = "걸음수는 0 이상이어야 합니다")
    private Integer totalSteps;

    /**
     * 분당 걸음수 (SPM)
     * 워치에서 계산된 값
     */
    @DecimalMin(value = "0.0", message = "분당 걸음수는 0 이상이어야 합니다")
    private Double stepsPerMinute;

    /**
     * 측정 시각
     */
    @NotNull(message = "측정 시각은 필수입니다")
    @PastOrPresent(message = "측정 시각은 현재 또는 과거여야 합니다")
    private LocalDateTime measuredAt;
}
