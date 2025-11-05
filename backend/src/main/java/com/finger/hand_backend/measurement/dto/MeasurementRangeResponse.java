package com.finger.hand_backend.measurement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 특정 기간 측정 데이터 조회 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class MeasurementRangeResponse {

    /**
     * 전체 측정 데이터 개수
     */
    private Integer total;

    /**
     * 시작 날짜
     */
    private LocalDate startDate;

    /**
     * 종료 날짜
     */
    private LocalDate endDate;

    /**
     * 측정 데이터 리스트
     */
    private List<MeasurementResponse> measurements;
}
