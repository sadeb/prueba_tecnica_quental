package com.quental.rickmorty.auth;

/** Token missing, malformed, tampered or expired. Always answered with 401 UNAUTHORIZED. */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
