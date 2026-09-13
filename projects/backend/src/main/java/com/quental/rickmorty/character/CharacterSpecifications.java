package com.quental.rickmorty.character;

import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

/** Filters of GET /api/characters; each returns null when the filter is absent (Specification tolerates null). */
public final class CharacterSpecifications {

    private CharacterSpecifications() {
    }

    public static Specification<Character> notPlaceholder() {
        return (root, query, cb) -> cb.isFalse(root.get("placeholder"));
    }

    public static Specification<Character> nameContains(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String pattern = "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Character> statusIs(CharacterStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Character> speciesIs(String species) {
        if (species == null || species.isBlank()) {
            return null;
        }
        String value = species.trim().toLowerCase(Locale.ROOT);
        return (root, query, cb) -> cb.equal(cb.lower(root.get("species")), value);
    }

    public static Specification<Character> genderIs(CharacterGender gender) {
        if (gender == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("gender"), gender);
    }
}
