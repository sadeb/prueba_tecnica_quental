package com.quental.rickmorty.character;

/** Optional filters of GET /api/characters; null means "no filter". */
public final class CharacterFilter {

    private final String name;
    private final CharacterStatus status;
    private final String species;
    private final CharacterGender gender;

    public CharacterFilter(String name, CharacterStatus status, String species, CharacterGender gender) {
        this.name = name;
        this.status = status;
        this.species = species;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public CharacterStatus getStatus() {
        return status;
    }

    public String getSpecies() {
        return species;
    }

    public CharacterGender getGender() {
        return gender;
    }
}
