package com.finger.hand_backend.baseline.dto;

import com.finger.hand_backend.baseline.Baseline;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Baseline 응답 DTO
 */
@Getter
@Builder
public class BaselineResponse {

    private Long id;
    private Integer version;
    private Boolean isActive;

    // HRV 통계
    private Double hrvSdnnMean;
    private Double hrvSdnnStd;
    private Double hrvRmssdMean;
    private Double hrvRmssdStd;

    // 심박수 통계
    private Double heartRateMean;
    private Double heartRateStd;

    // 체온 통계
    private Double objectTempMean;
    private Double objectTempStd;

    // 스트레스 임계값
    private Integer stressThresholdLow;
    private Integer stressThresholdMedium;
    private Integer stressThresholdHigh;

    // 메타데이터
    private Integer measurementCount;
    private LocalDate dataStartDate;
    private LocalDate dataEndDate;

    // 시간
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity → DTO 변환
     */
    public static BaselineResponse from(Baseline baseline) {
        return BaselineResponse.builder()
                .id(baseline.getId())
                .version(baseline.getVersion())
                .isActive(baseline.getIsActive())
                .hrvSdnnMean(baseline.getHrvSdnnMean())
                .hrvSdnnStd(baseline.getHrvSdnnStd())
                .hrvRmssdMean(baseline.getHrvRmssdMean())
                .hrvRmssdStd(baseline.getHrvRmssdStd())
                .heartRateMean(baseline.getHeartRateMean())
                .heartRateStd(baseline.getHeartRateStd())
                .objectTempMean(baseline.getObjectTempMean())
                .objectTempStd(baseline.getObjectTempStd())
                .stressThresholdLow(baseline.getStressThresholdLow())
                .stressThresholdMedium(baseline.getStressThresholdMedium())
                .stressThresholdHigh(baseline.getStressThresholdHigh())
                .measurementCount(baseline.getMeasurementCount())
                .dataStartDate(baseline.getDataStartDate())
                .dataEndDate(baseline.getDataEndDate())
                .createdAt(baseline.getCreatedAt())
                .updatedAt(baseline.getUpdatedAt())
                .build();
    }
}
