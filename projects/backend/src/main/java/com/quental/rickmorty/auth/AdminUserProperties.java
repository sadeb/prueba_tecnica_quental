package com.quental.rickmorty.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Credentials of the system administrator (ADR-012), provisioned at startup from
 * AUTH_ADMIN_USERNAME / AUTH_ADMIN_PASSWORD (.env). Same rules as RegisterRequest.
 */
@Validated
@ConfigurationProperties(prefix = "auth.admin")
public class AdminUserProperties {

    @NotBlank
    @Size(min = 3, max = 64)
    @Pattern(regexp = "[A-Za-z0-9._-]+")
    private String username;

    @NotBlank
    @Size(min = 8, max = 72)
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
