package com.finger.hand_backend.measurement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 측정 데이터 개수 조회 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class MeasurementCountResponse {

    /**
     * 측정 데이터 개수
     */
    private Long count;

    /**
     * 시작 날짜
     */
    private LocalDate startDate;

    /**
     * 종료 날짜
     */
    private LocalDate endDate;
}
