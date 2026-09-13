package com.quental.rickmorty.user;

/** Account role (ADR-012). Self-registered accounts are USER; the administrator from .env is ADMIN. */
public enum UserRole {
    USER,
    ADMIN
}
