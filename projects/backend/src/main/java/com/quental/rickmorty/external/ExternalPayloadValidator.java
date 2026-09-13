package com.quental.rickmorty.external;

import com.quental.rickmorty.common.InvalidExternalPayloadException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Structural checks before anything is mapped or published (spec/02 point 4). A page without
 * results is rejected as a whole; an invalid element is rejected individually so the producer can
 * skip it and continue (spec/03 point 4).
 */
@Component
public class ExternalPayloadValidator {

    public void validatePage(ExternalPage<?> page, String resource, int pageNumber) {
        if (page == null || page.getResults() == null) {
            throw new InvalidExternalPayloadException(
                    "Page " + pageNumber + " of " + resource + " has no 'results' array");
        }
    }

    public void validate(ExternalCharacter character) {
        requireId(character == null ? null : character.getId(), "character");
        requireName(character.getName(), "character", character.getId());
        parseAll(character.getEpisode(), "character " + character.getId() + " episode");
        if (character.getOrigin() != null) {
            ExternalIdParser.fromUrl(character.getOrigin().getUrl());
        }
        if (character.getLocation() != null) {
            ExternalIdParser.fromUrl(character.getLocation().getUrl());
        }
    }

    public void validate(ExternalEpisode episode) {
        requireId(episode == null ? null : episode.getId(), "episode");
        requireName(episode.getName(), "episode", episode.getId());
        parseAll(episode.getCharacters(), "episode " + episode.getId() + " character");
    }

    public void validate(ExternalLocation location) {
        requireId(location == null ? null : location.getId(), "location");
        requireName(location.getName(), "location", location.getId());
    }

    private static void requireId(Long id, String resource) {
        if (id == null || id <= 0) {
            throw new InvalidExternalPayloadException("Invalid " + resource + ": missing or non-positive id");
        }
    }

    private static void requireName(String name, String resource, Long id) {
        if (name == null || name.isBlank()) {
            throw new InvalidExternalPayloadException("Invalid " + resource + " " + id + ": blank name");
        }
    }

    private static void parseAll(List<String> urls, String context) {
        if (urls == null) {
            return;
        }
        for (String url : urls) {
            if (ExternalIdParser.fromUrl(url) == null) {
                throw new InvalidExternalPayloadException("Blank relation url in " + context);
            }
        }
    }
}
