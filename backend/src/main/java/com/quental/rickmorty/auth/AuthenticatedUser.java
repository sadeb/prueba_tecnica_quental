package com.quental.rickmorty.auth;

import com.quental.rickmorty.auth.domain.UserRole;

public class AuthenticatedUser {
    private final Long id;
    private final String username;
    private final UserRole role;

    public AuthenticatedUser(Long id, String username, UserRole role) {
        this.id = id; this.username = username; this.role = role;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
}
