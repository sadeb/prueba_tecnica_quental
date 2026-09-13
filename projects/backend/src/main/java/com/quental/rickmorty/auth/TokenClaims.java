package com.quental.rickmorty.auth;

import java.time.Instant;

public final class TokenClaims {

    private final long userId;
    private final String username;
    private final Instant issuedAt;
    private final Instant expiresAt;

    public TokenClaims(long userId, String username, Instant issuedAt, Instant expiresAt) {
        this.userId = userId;
        this.username = username;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
