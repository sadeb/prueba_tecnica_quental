package com.quental.rickmorty.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.auth")
public class AuthProperties {

    private Duration tokenTtl = Duration.ofHours(8);
    private String adminUsername = "";
    private String adminPassword = "";

    public Duration getTokenTtl() { return tokenTtl; }
    public void setTokenTtl(Duration tokenTtl) { this.tokenTtl = tokenTtl; }
    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
}
