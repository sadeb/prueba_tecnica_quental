package com.quental.rickmorty.auth;

import com.quental.rickmorty.user.UserRole;

import java.time.Instant;

public final class TokenClaims {

    private final long userId;
    private final String username;
    private final UserRole role;
    private final Instant issuedAt;
    private final Instant expiresAt;

    public TokenClaims(long userId, String username, UserRole role, Instant issuedAt, Instant expiresAt) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
