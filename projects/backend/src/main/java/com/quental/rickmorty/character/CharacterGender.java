package com.quental.rickmorty.character;

import java.util.Locale;

public enum CharacterGender {
    FEMALE, MALE, GENDERLESS, UNKNOWN;

    public static CharacterGender fromValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public static CharacterGender fromExternal(String value) {
        try {
            return fromValue(value);
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}
