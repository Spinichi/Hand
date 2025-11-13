package com.finger.hand_backend.group.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 그룹 이상치 통계 응답
 */
@Getter
@Builder
public class GroupAnomalyStatisticsResponse {

    /**
     * 그룹 ID
     */
    private Long groupId;

    /**
     * 그룹 이름
     */
    private String groupName;

    /**
     * 멤버 수 (매니저 제외)
     */
    private Integer memberCount;

    /**
     * 주간 이상치 통계
     */
    private WeeklyAnomalyStatistics weeklyStatistics;

    /**
     * 최고 위험 멤버
     */
    private TopRiskMember topRiskMember;
}
