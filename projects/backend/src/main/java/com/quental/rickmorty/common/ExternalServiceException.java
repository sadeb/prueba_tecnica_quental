package com.quental.rickmorty.common;

/** The external source answered with an unexpected status or could not be reached after retries. */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
