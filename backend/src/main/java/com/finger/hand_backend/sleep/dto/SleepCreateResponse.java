package com.finger.hand_backend.sleep.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 수면 데이터 저장 응답 DTO
 */
@Getter
@Builder
public class SleepCreateResponse {
    private Long id;
    private Integer sleepDurationMinutes;
    private Integer sleepDurationHours;
}