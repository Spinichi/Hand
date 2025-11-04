package com.finger.hand_backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {
    private String accessToken;
    private long expiresIn; // seconds

    public TokenResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}

