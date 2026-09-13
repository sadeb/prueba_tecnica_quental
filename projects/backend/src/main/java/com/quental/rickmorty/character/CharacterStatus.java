package com.quental.rickmorty.character;

import java.util.Locale;

public enum CharacterStatus {
    ALIVE, DEAD, UNKNOWN;

    /** Tolerant parse used for external data and query params ("Alive", "alive", "ALIVE"). */
    public static CharacterStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public static CharacterStatus fromExternal(String value) {
        try {
            return fromValue(value);
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}
