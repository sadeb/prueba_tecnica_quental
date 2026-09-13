package com.quental.rickmorty.sync.message;

/** Message that can never be processed (bad JSON, unknown schema, invalid snapshot): no retries, straight to DLT. */
public class InvalidMessageException extends RuntimeException {

    public InvalidMessageException(String message) {
        super(message);
    }

    public InvalidMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
