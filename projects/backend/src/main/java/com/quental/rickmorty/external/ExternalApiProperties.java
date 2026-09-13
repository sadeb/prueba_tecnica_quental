package com.quental.rickmorty.external;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "external-api")
public class ExternalApiProperties {

    @NotBlank
    private String baseUrl = "https://rickandmortyapi.com/api";
    @NotNull
    private Duration connectTimeout = Duration.ofSeconds(5);
    @NotNull
    private Duration readTimeout = Duration.ofSeconds(10);
    /** Extra attempts after the first one, for timeouts and 5xx. */
    @Min(0)
    private int maxRetries = 3;
    @NotNull
    private Duration retryBackoff = Duration.ofMillis(500);

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Duration getRetryBackoff() {
        return retryBackoff;
    }

    public void setRetryBackoff(Duration retryBackoff) {
        this.retryBackoff = retryBackoff;
    }
}
