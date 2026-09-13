package com.quental.rickmorty.shared;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

public class ApiError {

    private final Instant timestamp;
    private final int status;
    private final String code;
    private final String message;
    private final String path;
    private final String traceId;
    private final Map<String, String> fieldErrors;

    public ApiError(int status, String code, String message, String path, String traceId, Map<String, String> fieldErrors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.traceId = traceId;
        this.fieldErrors = fieldErrors == null ? Collections.emptyMap() : fieldErrors;
    }

    public Instant getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getCode() { return code; }
    public String getMessage() { return message; }
    public String getPath() { return path; }
    public String getTraceId() { return traceId; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
}
