package com.finger.hand_backend.group.dto;

public record InviteValidationResponse(boolean valid, Long groupId, String groupName) {}
