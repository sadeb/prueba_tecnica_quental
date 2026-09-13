package com.quental.rickmorty.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Single error body for every 4xx/5xx response of the API (conventions/formato-error.md).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiError {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<FieldDetail> details;

    public ApiError(Instant timestamp, int status, String error, String message, String path, List<FieldDetail> details) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.details = details == null || details.isEmpty() ? null : List.copyOf(details);
    }

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path, null);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<FieldDetail> getDetails() {
        return details;
    }

    public static final class FieldDetail {
        private final String field;
        private final String message;

        public FieldDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}
