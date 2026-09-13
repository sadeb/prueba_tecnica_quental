package com.quental.rickmorty.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Schema(name = "RegisterRequest")
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 64)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "only letters, digits, '.', '_' and '-' are allowed")
    @Schema(example = "rick")
    private String username;

    @NotBlank
    @Size(min = 8, max = 72)
    @Schema(example = "wubbalubba", minLength = 8)
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
