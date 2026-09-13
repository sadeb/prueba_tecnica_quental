package com.quental.rickmorty.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.topics")
public class TopicProperties {

    private String raw = "rickmorty.raw.v1";
    private String graph = "rickmorty.graph.v1";
    private String deadLetterSuffix = ".DLT";

    public String getRaw() { return raw; }
    public void setRaw(String raw) { this.raw = raw; }
    public String getGraph() { return graph; }
    public void setGraph(String graph) { this.graph = graph; }
    public String getDeadLetterSuffix() { return deadLetterSuffix; }
    public void setDeadLetterSuffix(String deadLetterSuffix) { this.deadLetterSuffix = deadLetterSuffix; }
}
