package com.quental.rickmorty.auth.api;

import java.time.Instant;

public class TokenResponse {
    private final String token;
    private final Instant expiresAt;
    private final UserResponse user;

    public TokenResponse(String token, Instant expiresAt, UserResponse user) {
        this.token = token; this.expiresAt = expiresAt; this.user = user;
    }
    public String getToken() { return token; }
    public Instant getExpiresAt() { return expiresAt; }
    public UserResponse getUser() { return user; }
}
