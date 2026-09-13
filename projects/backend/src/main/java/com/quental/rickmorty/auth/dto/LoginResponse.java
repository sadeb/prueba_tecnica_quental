package com.quental.rickmorty.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "LoginResponse")
public final class LoginResponse {

    @Schema(description = "Send as 'Authorization: Bearer <token>'")
    private final String token;
    private final Instant expiresAt;
    private final String username;

    public LoginResponse(String token, Instant expiresAt, String username) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getUsername() {
        return username;
    }
}
