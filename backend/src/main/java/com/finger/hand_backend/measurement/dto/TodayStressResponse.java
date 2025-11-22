package com.finger.hand_backend.measurement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TodayStressResponse(
        LocalDate date,
        int anomalyCount,
        List<HourlyStatsDto> hourlyStats,
        List<StressPointDto> peakStress,
        List<StressPointDto> lowestStress,
        Integer peakFrequencyHour,      // 측정 빈도가 가장 높은 시간대 (0~23)
        Integer peakFrequencyCount      // 해당 시간대의 측정 횟수
) {}