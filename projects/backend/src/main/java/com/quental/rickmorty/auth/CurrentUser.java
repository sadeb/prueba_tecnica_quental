package com.quental.rickmorty.auth;

import com.quental.rickmorty.common.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Access to the authenticated user from controllers without relying on argument resolvers. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthenticatedUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw new UnauthorizedException("No authenticated user");
        }
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
