package com.finger.hand_backend.group.dto;

import jakarta.validation.constraints.Size;

public record UpdateMemberNotesRequest(@Size(max=2000) String specialNotes) {}
