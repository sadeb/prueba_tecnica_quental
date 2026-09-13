package com.quental.rickmorty.common;

/** The external source answered, but the body does not match the expected contract. */
public class InvalidExternalPayloadException extends RuntimeException {

    public InvalidExternalPayloadException(String message) {
        super(message);
    }

    public InvalidExternalPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
