package com.quental.rickmorty.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegisterResponse")
public final class RegisterResponse {

    private final Long id;
    private final String username;

    public RegisterResponse(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
