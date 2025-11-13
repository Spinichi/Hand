package com.finger.hand_backend.measurement.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 특정 날짜의 이상치 응답 DTO
 */
@Getter
@Builder
public class DailyAnomalyResponse {

    /**
     * 날짜
     */
    private LocalDate date;

    /**
     * 이상치 개수
     */
    private Integer anomalyCount;

    /**
     * 이상치 측정 데이터 목록
     */
    private List<MeasurementResponse> anomalies;
}
