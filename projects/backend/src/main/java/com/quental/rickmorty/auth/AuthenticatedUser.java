package com.quental.rickmorty.auth;

/** Principal placed in the SecurityContext by BearerTokenFilter. */
public final class AuthenticatedUser {

    private final long id;
    private final String username;

    public AuthenticatedUser(long id, String username) {
        this.id = id;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return username;
    }
}
