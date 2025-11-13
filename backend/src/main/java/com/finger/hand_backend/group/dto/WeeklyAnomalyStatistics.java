package com.finger.hand_backend.group.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * 주간 이상치 통계
 */
@Getter
@Builder
public class WeeklyAnomalyStatistics {

    /**
     * 조회 시작 날짜
     */
    private LocalDate startDate;

    /**
     * 조회 종료 날짜
     */
    private LocalDate endDate;

    /**
     * 주간 전체 평균 이상치 횟수
     */
    private Double totalAverageAnomalyCount;

    /**
     * 일별 평균 이상치 횟수
     */
    private List<DailyAverageAnomaly> dailyAverages;
}
