package com.quental.rickmorty.common;

/** Bad credentials or no authenticated user in a context that requires one. Answered with 401. */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
