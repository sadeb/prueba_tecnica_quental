package com.quental.rickmorty.auth;

import java.time.Instant;

public class IssuedToken {
    private final String value;
    private final Instant expiresAt;

    public IssuedToken(String value, Instant expiresAt) {
        this.value = value; this.expiresAt = expiresAt;
    }
    public String getValue() { return value; }
    public Instant getExpiresAt() { return expiresAt; }
}
