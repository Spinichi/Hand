package com.finger.hand_backend.group.dto;

import java.time.Instant;

public record GroupResponse(Long id, String name, String groupType,
                            String inviteCode, Long createdBy,
                            Instant createdAt, Instant updatedAt,
                            Double avgMemberRiskScore, Integer memberCount) {}
