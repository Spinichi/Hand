package com.finger.hand_backend.measurement.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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
     */
    @NotNull(message = "심박수는 필수입니다")
    @Min(value = 30, message = "심박수는 30 이상이어야 합니다")
    @Max(value = 200, message = "심박수는 200 이하여야 합니다")
    private Integer heartRate;

    /**
     * IBI 배열 (Inter-Beat Interval, ms 단위)
     * 최소 5개 이상 필요 (HRV 계산을 위해)
     */
    @NotNull(message = "IBI 배열은 필수입니다")
    @Size(min = 5, message = "IBI 배열은 최소 5개 이상이어야 합니다")
    private List<Integer> ibiArray;

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
     * 가속도계 X축
     */
    private Integer accelX;

    /**
     * 가속도계 Y축
     */
    private Integer accelY;

    /**
     * 가속도계 Z축
     */
    private Integer accelZ;

    /**
     * 누적 걸음수 (부팅 후)
     * Optional: 활동 감지용
     */
    @Min(value = 0, message = "걸음수는 0 이상이어야 합니다")
    private Integer totalSteps;

    /**
     * 가장 최근 걸음 시간
     * Optional: 활동 감지용
     */
    @PastOrPresent(message = "최근 걸음 시간은 현재 또는 과거여야 합니다")
    private LocalDateTime lastStepAt;

    /**
     * 측정 시각
     */
    @NotNull(message = "측정 시각은 필수입니다")
    @PastOrPresent(message = "측정 시각은 현재 또는 과거여야 합니다")
    private LocalDateTime measuredAt;
}
