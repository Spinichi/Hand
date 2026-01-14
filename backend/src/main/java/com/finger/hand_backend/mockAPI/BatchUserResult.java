package com.finger.hand_backend.mockAPI;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class BatchUserResult {
    private final Long userId;
    private final boolean success;
    private final String errorType;

    public static BatchUserResult success(Long userId) {
        return of(userId, true, null);
    }

    public static BatchUserResult fail(Long userId, String errorType) {
        return of(userId, false, errorType);
    }
}

