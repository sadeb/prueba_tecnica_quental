package com.quental.rickmorty.auth.api;

import com.quental.rickmorty.auth.domain.UserEntity;
import java.time.Instant;

public class AdminUserResponse {
    private final Long id;
    private final String username;
    private final String role;
    private final boolean enabled;
    private final Instant createdAt;

    public AdminUserResponse(Long id, String username, String role, boolean enabled, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    public static AdminUserResponse from(UserEntity user) {
        return new AdminUserResponse(user.getId(), user.getUsername(), user.getRole().name(),
                user.isEnabled(), user.getCreatedAt());
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
}
