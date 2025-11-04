package com.finger.hand_backend.group.dto;

import jakarta.validation.constraints.*;

public record CreateGroupRequest(@NotBlank @Size(max=100) String name,
                                 @NotBlank @Size(max=50)  String groupType) {}

