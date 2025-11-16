package com.finger.hand_backend.sleep.dto;

import com.finger.hand_backend.sleep.Sleep;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 수면 데이터 응답 DTO
 */
@Getter
@Builder
public class SleepResponse {

    private Long id;
    private LocalDateTime sleepStartTime;
    private LocalDateTime sleepEndTime;
    private Integer sleepDurationMinutes;
    private Integer sleepDurationHours;  // 시간 단위 (UI 편의성)
    private LocalDate sleepDate;
    private LocalDateTime createdAt;

    /**
     * Entity → DTO 변환
     */
    public static SleepResponse from(Sleep sleep) {
        return SleepResponse.builder()
            .id(sleep.getId())
            .sleepStartTime(sleep.getSleepStartTime())
            .sleepEndTime(sleep.getSleepEndTime())
            .sleepDurationMinutes(sleep.getSleepDurationMinutes())
            .sleepDurationHours(sleep.getSleepDurationMinutes() / 60)
            .sleepDate(sleep.getSleepDate())
            .createdAt(sleep.getCreatedAt())
            .build();
    }
}