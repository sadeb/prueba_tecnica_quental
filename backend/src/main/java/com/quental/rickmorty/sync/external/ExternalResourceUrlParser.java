package com.quental.rickmorty.sync.external;

import java.net.URI;
import java.util.Optional;

public final class ExternalResourceUrlParser {

    private ExternalResourceUrlParser() {
    }

    public static Optional<Long> extractId(String value, ResourceType expectedType) {
        if (value == null || value.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(value);
            String[] segments = uri.getPath().split("/");
            if (segments.length < 3 || !expectedType.getPath().equals(segments[segments.length - 2])) {
                throw new IllegalArgumentException("Unexpected resource URL: " + value);
            }
            long id = Long.parseLong(segments[segments.length - 1]);
            if (id <= 0) {
                throw new IllegalArgumentException("External id must be positive: " + value);
            }
            return Optional.of(id);
        } catch (RuntimeException exception) {
            throw new ExternalApiException("Invalid " + expectedType.name().toLowerCase() + " URL", exception);
        }
    }
}
