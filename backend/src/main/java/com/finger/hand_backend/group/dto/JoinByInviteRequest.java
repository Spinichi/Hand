package com.finger.hand_backend.group.dto;

import jakarta.validation.constraints.Pattern;

public record JoinByInviteRequest(@Pattern(regexp="^[A-Z0-9]{6}$") String inviteCode) {}