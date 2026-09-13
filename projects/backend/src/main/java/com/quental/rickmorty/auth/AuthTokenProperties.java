package com.quental.rickmorty.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Validated
@ConfigurationProperties(prefix = "auth.token")
public class AuthTokenProperties {

    /** HMAC-SHA256 key; at least 32 characters (AUTH_TOKEN_SECRET in .env). */
    @NotBlank
    @Size(min = 32)
    private String secret;

    @Min(1)
    private long ttlHours = 8;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getTtlHours() {
        return ttlHours;
    }

    public void setTtlHours(long ttlHours) {
        this.ttlHours = ttlHours;
    }
}
