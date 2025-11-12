package com.finger.hand_backend.measurement.dto;

import com.finger.hand_backend.measurement.Measurement;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 측정 데이터 응답 DTO
 */
@Getter
@Builder
public class MeasurementResponse {

    private Long id;
    private Double heartRate;
    private Double hrvSdnn;
    private Double hrvRmssd;
    private Double objectTemp;
    private Double ambientTemp;
    private Double accelX;
    private Double accelY;
    private Double accelZ;
    private Double movementIntensity;
    private Double stressIndex;
    private Integer stressLevel;
    private Boolean isAnomaly;

    // 활동 감지
    private Integer totalSteps;
    private Double stepsPerMinute;

    private LocalDateTime measuredAt;
    private LocalDateTime createdAt;

    /**
     * Entity → DTO 변환
     */
    public static MeasurementResponse from(Measurement measurement) {
        return MeasurementResponse.builder()
            .id(measurement.getId())
            .heartRate(measurement.getHeartRate())
            .hrvSdnn(measurement.getHrvSdnn())
            .hrvRmssd(measurement.getHrvRmssd())
            .objectTemp(measurement.getObjectTemp())
            .ambientTemp(measurement.getAmbientTemp())
            .accelX(measurement.getAccelX())
            .accelY(measurement.getAccelY())
            .accelZ(measurement.getAccelZ())
            .movementIntensity(measurement.getMovementIntensity())
            .stressIndex(measurement.getStressIndex())
            .stressLevel(measurement.getStressLevel())
            .isAnomaly(measurement.getIsAnomaly())
            .totalSteps(measurement.getTotalSteps())
            .stepsPerMinute(measurement.getStepsPerMinute())
            .measuredAt(measurement.getMeasuredAt())
            .createdAt(measurement.getCreatedAt())
            .build();
    }
}
