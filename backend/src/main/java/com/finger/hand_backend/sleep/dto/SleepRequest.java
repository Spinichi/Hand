package com.finger.hand_backend.sleep.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 수면 데이터 저장 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleepRequest {

    /**
     * 수면 시작 시간 (ISO-8601 형식)
     * 예: "2025-01-14T22:30:00"
     */
    @NotNull(message = "수면 시작 시간은 필수입니다")
    private LocalDateTime sleepStartTime;

    /**
     * 수면 종료 시간 (ISO-8601 형식)
     * 예: "2025-01-15T07:00:00"
     */
    @NotNull(message = "수면 종료 시간은 필수입니다")
    private LocalDateTime sleepEndTime;
}