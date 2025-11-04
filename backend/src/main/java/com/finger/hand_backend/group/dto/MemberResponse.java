package com.finger.hand_backend.group.dto;

import com.finger.hand_backend.group.entity.GroupRole;
import java.time.Instant;

public record MemberResponse(Long userId, GroupRole role,
                             String specialNotes, Instant joinedAt) {}
