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
    private Integer heartRate;
    private Double hrvSdnn;
    private Double hrvRmssd;
    private Double objectTemp;
    private Double ambientTemp;
    private Integer accelX;
    private Integer accelY;
    private Integer accelZ;
    private Double movementIntensity;
    private Integer stressIndex;  // 1-100
    private Integer stressLevel;  // 1-5

    // 활동 감지
    private Integer totalSteps;
    private LocalDateTime lastStepAt;
    private Double stepsPerMinute;
    private String activityState;  // STATIC | WALKING
    private String measurementQuality;  // HIGH | LOW

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
            .totalSteps(measurement.getTotalSteps())
            .lastStepAt(measurement.getLastStepAt())
            .stepsPerMinute(measurement.getStepsPerMinute())
            .activityState(measurement.getActivityState())
            .measurementQuality(measurement.getMeasurementQuality())
            .measuredAt(measurement.getMeasuredAt())
            .createdAt(measurement.getCreatedAt())
            .build();
    }
}
