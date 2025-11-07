package com.finger.hand_backend.baseline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Baseline 페이징 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class BaselinePageResponse {

    /**
     * 전체 Baseline 개수
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
     * Baseline 리스트
     */
    private List<BaselineResponse> baselines;
}
