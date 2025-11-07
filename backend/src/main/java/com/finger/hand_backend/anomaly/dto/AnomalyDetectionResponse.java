package com.finger.hand_backend.anomaly.dto;

import com.finger.hand_backend.anomaly.AnomalyDetection;
import com.finger.hand_backend.measurement.Measurement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 이상치 탐지 응답 DTO
 * - AnomalyDetection + Measurement 정보 포함
 */
@Getter
@Builder
@AllArgsConstructor
public class AnomalyDetectionResponse {

    /**
     * 이상치 ID
     */
    private Long id;

    /**
     * 측정 데이터 ID
     */
    private Long measurementId;

    /**
     * 스트레스 지수 (1-100)
     */
    private Integer stressIndex;

    /**
     * 스트레스 단계 (1-5)
     */
    private Integer stressLevel;

    /**
     * 심박수 (bpm)
     */
    private Integer heartRate;

    /**
     * HRV SDNN (ms)
     */
    private Double hrvSdnn;

    /**
     * HRV RMSSD (ms)
     */
    private Double hrvRmssd;

    /**
     * 피부 온도 (°C)
     */
    private Double objectTemp;

    /**
     * 활동 상태 (STATIC | WALKING)
     */
    private String activityState;

    /**
     * 측정 품질 (HIGH | LOW)
     */
    private String measurementQuality;

    /**
     * 측정 시각
     */
    private LocalDateTime measuredAt;

    /**
     * 이상치 감지 시각
     */
    private LocalDateTime createdAt;

    /**
     * Entity + Measurement → DTO 변환
     *
     * @param anomaly     이상치 엔티티
     * @param measurement 측정 데이터 엔티티
     * @return AnomalyDetectionResponse
     */
    public static AnomalyDetectionResponse from(AnomalyDetection anomaly, Measurement measurement) {
        return AnomalyDetectionResponse.builder()
                .id(anomaly.getId())
                .measurementId(anomaly.getMeasurementId())
                .stressIndex(measurement.getStressIndex())
                .stressLevel(measurement.getStressLevel())
                .heartRate(measurement.getHeartRate())
                .hrvSdnn(measurement.getHrvSdnn())
                .hrvRmssd(measurement.getHrvRmssd())
                .objectTemp(measurement.getObjectTemp())
                .activityState(measurement.getActivityState())
                .measurementQuality(measurement.getMeasurementQuality())
                .measuredAt(measurement.getMeasuredAt())
                .createdAt(anomaly.getCreatedAt())
                .build();
    }
}
