package com.quental.rickmorty.sync.external;

public enum ResourceType {
    LOCATION("location"),
    EPISODE("episode"),
    CHARACTER("character");

    private final String path;

    ResourceType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
