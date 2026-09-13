package com.quental.rickmorty.auth.api;

import com.quental.rickmorty.auth.domain.UserEntity;

public class UserResponse {
    private final Long id;
    private final String username;
    private final String role;

    public UserResponse(Long id, String username, String role) {
        this.id = id; this.username = username; this.role = role;
    }
    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole().name());
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
