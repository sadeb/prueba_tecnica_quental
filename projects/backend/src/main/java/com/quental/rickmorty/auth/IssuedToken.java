package com.quental.rickmorty.auth;

import java.time.Instant;

public final class IssuedToken {

    private final String token;
    private final Instant expiresAt;

    public IssuedToken(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
