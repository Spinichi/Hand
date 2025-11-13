package com.finger.hand_backend.measurement.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 일주일 이상치 응답 DTO (날짜별 그룹화)
 */
@Getter
@Builder
public class WeeklyAnomalyResponse {

    /**
     * 조회 시작 날짜
     */
    private LocalDate startDate;

    /**
     * 조회 종료 날짜
     */
    private LocalDate endDate;

    /**
     * 전체 이상치 개수
     */
    private Integer totalAnomalyCount;

    /**
     * 날짜별 이상치 데이터
     */
    private List<DailyAnomalyResponse> dailyAnomalies;
}
