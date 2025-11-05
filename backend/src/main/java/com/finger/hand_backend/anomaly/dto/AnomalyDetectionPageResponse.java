package com.finger.hand_backend.anomaly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 이상치 페이징 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class AnomalyDetectionPageResponse {

    /**
     * 전체 이상치 개수
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
     * 이상치 리스트
     */
    private List<AnomalyDetectionResponse> anomalies;
}
