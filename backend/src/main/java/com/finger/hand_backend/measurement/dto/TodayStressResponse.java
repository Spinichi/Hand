package com.finger.hand_backend.measurement.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TodayStressResponse(
        LocalDate date,
        int anomalyCount,
        List<HourlyStatsDto> hourlyStats,
        List<StressPointDto> peakStress,
        List<StressPointDto> lowestStress
) {}