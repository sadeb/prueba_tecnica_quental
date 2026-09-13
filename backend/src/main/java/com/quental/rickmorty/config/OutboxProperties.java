package com.quental.rickmorty.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.outbox")
public class OutboxProperties {

    private boolean enabled = true;
    private int batchSize = 100;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
}
