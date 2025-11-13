package com.finger.hand_backend.group.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 최고 위험 멤버 정보
 */
@Getter
@Builder
public class TopRiskMember {

    /**
     * 사용자 ID
     */
    private Long userId;

    /**
     * 사용자 이름
     */
    private String userName;

    /**
     * 주간 평균 이상치 횟수
     */
    private Double weeklyAverageAnomalyCount;
}
