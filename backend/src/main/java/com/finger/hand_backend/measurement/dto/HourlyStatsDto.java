package com.finger.hand_backend.measurement.dto;

public record HourlyStatsDto(
        int hour,
        Double maxStress,
        Double minStress,
        Double avgStress,
        int measurementCount
) {}
