package com.quental.rickmorty.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.cors")
public class CorsProperties {

    private List<String> allowedOrigins = new ArrayList<>(Arrays.asList(
            "http://localhost:4200",
            "http://localhost:4400"));

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }
}
