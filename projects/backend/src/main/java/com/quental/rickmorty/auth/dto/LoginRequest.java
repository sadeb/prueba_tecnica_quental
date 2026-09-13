package com.quental.rickmorty.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Schema(name = "LoginRequest")
public class LoginRequest {

    @NotBlank
    @Schema(example = "rick")
    private String username;

    @NotBlank
    @Schema(example = "wubbalubba")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
