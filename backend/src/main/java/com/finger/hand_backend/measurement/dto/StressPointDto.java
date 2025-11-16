package com.finger.hand_backend.measurement.dto;

import java.time.LocalDateTime;

public record StressPointDto(
        Double stressIndex,
        LocalDateTime measuredAt
) {}
