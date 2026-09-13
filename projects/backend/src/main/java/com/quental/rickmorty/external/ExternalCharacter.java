package com.quental.rickmorty.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalCharacter {

    private Long id;
    private String name;
    private String status;
    private String species;
    private String type;
    private String gender;
    private ExternalReference origin;
    private ExternalReference location;
    private String image;
    private List<String> episode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public ExternalReference getOrigin() {
        return origin;
    }

    public void setOrigin(ExternalReference origin) {
        this.origin = origin;
    }

    public ExternalReference getLocation() {
        return location;
    }

    public void setLocation(ExternalReference location) {
        this.location = location;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getEpisode() {
        return episode;
    }

    public void setEpisode(List<String> episode) {
        this.episode = episode;
    }
}
