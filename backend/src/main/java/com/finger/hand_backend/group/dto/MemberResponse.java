package com.finger.hand_backend.group.dto;

import com.finger.hand_backend.group.entity.GroupRole;
import java.time.Instant;

public record MemberResponse(Long userId, String name, GroupRole role,
                             String specialNotes, Instant joinedAt,
                             Double weeklyAvgRiskScore) {}
