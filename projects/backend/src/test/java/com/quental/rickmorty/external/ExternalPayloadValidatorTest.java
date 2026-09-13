package com.quental.rickmorty.external;

import com.quental.rickmorty.common.InvalidExternalPayloadException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalPayloadValidatorTest {

    private final ExternalPayloadValidator validator = new ExternalPayloadValidator();

    @Test
    void shouldRejectPageWithoutResults() {
        ExternalPage<ExternalCharacter> page = new ExternalPage<>();

        assertThatThrownBy(() -> validator.validatePage(page, "character", 3))
                .isInstanceOf(InvalidExternalPayloadException.class)
                .hasMessageContaining("Page 3");
    }

    @Test
    void shouldRejectCharacterWithNonPositiveId() {
        ExternalCharacter character = character(0L, "Rick", List.of());

        assertThatThrownBy(() -> validator.validate(character)).isInstanceOf(InvalidExternalPayloadException.class);
    }

    @Test
    void shouldRejectCharacterWithBlankName() {
        ExternalCharacter character = character(1L, "  ", List.of());

        assertThatThrownBy(() -> validator.validate(character))
                .isInstanceOf(InvalidExternalPayloadException.class)
                .hasMessageContaining("blank name");
    }

    @Test
    void shouldRejectCharacterWhenEpisodeUrlIsNotNumeric() {
        ExternalCharacter character = character(1L, "Rick", List.of("https://x/api/episode/abc"));

        assertThatThrownBy(() -> validator.validate(character))
                .isInstanceOf(InvalidExternalPayloadException.class)
                .hasMessageContaining("Non-numeric");
    }

    @Test
    void shouldAcceptCharacterWithUnknownOriginAndNoEpisodes() {
        ExternalCharacter character = character(1L, "Rick", List.of());
        ExternalReference unknown = new ExternalReference();
        unknown.setName("unknown");
        unknown.setUrl("");
        character.setOrigin(unknown);

        assertThatCode(() -> validator.validate(character)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectEpisodeWithMissingId() {
        ExternalEpisode episode = new ExternalEpisode();
        episode.setName("Pilot");

        assertThatThrownBy(() -> validator.validate(episode)).isInstanceOf(InvalidExternalPayloadException.class);
    }

    @Test
    void shouldRejectLocationWithBlankName() {
        ExternalLocation location = new ExternalLocation();
        location.setId(5L);
        location.setName("");

        assertThatThrownBy(() -> validator.validate(location)).isInstanceOf(InvalidExternalPayloadException.class);
    }

    private static ExternalCharacter character(Long id, String name, List<String> episodes) {
        ExternalCharacter character = new ExternalCharacter();
        character.setId(id);
        character.setName(name);
        character.setEpisode(episodes);
        return character;
    }
}
