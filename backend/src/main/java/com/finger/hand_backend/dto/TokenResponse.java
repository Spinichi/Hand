package com.finger.hand_backend.dto;

public class TokenResponse {
    private String accessToken;
    private long expiresIn; // seconds

    public TokenResponse(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
    public String getAccessToken() { return accessToken; }
    public long getExpiresIn() { return expiresIn; }
}

