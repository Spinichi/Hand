package com.finger.hand_backend.measurement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 측정 데이터 페이징 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class MeasurementPageResponse {

    /**
     * 전체 측정 데이터 개수
     */
    private Long total;

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private Integer page;

    /**
     * 페이지 크기
     */
    private Integer size;

    /**
     * 측정 데이터 리스트
     */
    private List<MeasurementResponse> measurements;
}
