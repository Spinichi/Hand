package com.finger.hand_backend.measurement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 측정 데이터 생성 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class MeasurementCreateResponse {

    /**
     * 측정 데이터 ID
     */
    private Long id;

    /**
     * 스트레스 지수 (0-100)
     */
    private Double stressIndex;

    /**
     * 스트레스 단계 (1-5)
     */
    private Integer stressLevel;

    /**
     * 이상치 여부
     */
    private Boolean isAnomaly;
}
