package com.finger.hand_backend.group.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 일별 평균 이상치 횟수
 */
@Getter
@Builder
public class DailyAverageAnomaly {

    /**
     * 날짜
     */
    private LocalDate date;

    /**
     * 해당 날짜의 그룹 멤버 평균 이상치 횟수
     */
    private Double averageAnomalyCount;
}
