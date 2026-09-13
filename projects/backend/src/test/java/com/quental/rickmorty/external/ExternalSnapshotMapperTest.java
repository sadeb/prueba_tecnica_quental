package com.quental.rickmorty.external;

import com.quental.rickmorty.sync.message.CharacterSnapshot;
import com.quental.rickmorty.sync.message.EpisodeSnapshot;
import com.quental.rickmorty.sync.message.LocationSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalSnapshotMapperTest {

    private final ExternalSnapshotMapper mapper = new ExternalSnapshotMapper();

    @Test
    void shouldNormaliseEmptyStringsToNullAndExtractIdsFromUrls() {
        ExternalCharacter source = new ExternalCharacter();
        source.setId(1L);
        source.setName("Rick Sanchez");
        source.setStatus("Alive");
        source.setSpecies("Human");
        source.setType("");
        source.setGender("Male");
        source.setImage("https://img/1.jpeg");
        source.setOrigin(reference("Earth (C-137)", "https://x/api/location/1"));
        source.setLocation(reference("Citadel of Ricks", "https://x/api/location/3"));
        source.setEpisode(List.of("https://x/api/episode/1", "https://x/api/episode/2", "https://x/api/episode/2"));

        CharacterSnapshot snapshot = mapper.toSnapshot(source);

        assertThat(snapshot.getExternalId()).isEqualTo(1L);
        assertThat(snapshot.getType()).isNull();
        assertThat(snapshot.getStatus()).isEqualTo("ALIVE");
        assertThat(snapshot.getGender()).isEqualTo("MALE");
        assertThat(snapshot.getOriginExternalId()).isEqualTo(1L);
        assertThat(snapshot.getOriginName()).isEqualTo("Earth (C-137)");
        assertThat(snapshot.getLocationExternalId()).isEqualTo(3L);
        assertThat(snapshot.getEpisodeExternalIds()).containsExactly(1L, 2L);
    }

    @Test
    void shouldLeaveOriginNullWhenSourceHasNoUrl() {
        ExternalCharacter source = minimalCharacter();
        source.setOrigin(reference("unknown", ""));
        source.setLocation(null);

        CharacterSnapshot snapshot = mapper.toSnapshot(source);

        assertThat(snapshot.getOriginExternalId()).isNull();
        assertThat(snapshot.getOriginName()).isNull();
        assertThat(snapshot.getLocationExternalId()).isNull();
        assertThat(snapshot.getEpisodeExternalIds()).isEmpty();
    }

    @Test
    void shouldMapUnrecognisedStatusAndGenderToUnknown() {
        ExternalCharacter source = minimalCharacter();
        source.setStatus("Zombie");
        source.setGender("unknown");

        CharacterSnapshot snapshot = mapper.toSnapshot(source);

        assertThat(snapshot.getStatus()).isEqualTo("UNKNOWN");
        assertThat(snapshot.getGender()).isEqualTo("UNKNOWN");
    }

    @Test
    void shouldKeepAirDateAsTextAndMapCodeFromEpisodeField() {
        ExternalEpisode source = new ExternalEpisode();
        source.setId(28L);
        source.setName("The Ricklantis Mixup");
        source.setAirDate("September 10, 2017");
        source.setEpisode("S03E07");
        source.setCharacters(List.of("https://x/api/character/1", "https://x/api/character/2"));

        EpisodeSnapshot snapshot = mapper.toSnapshot(source);

        assertThat(snapshot.getAirDate()).isEqualTo("September 10, 2017");
        assertThat(snapshot.getCode()).isEqualTo("S03E07");
        assertThat(snapshot.getCharacterExternalIds()).containsExactly(1L, 2L);
    }

    @Test
    void shouldNormaliseEmptyDimensionToNull() {
        ExternalLocation source = new ExternalLocation();
        source.setId(7L);
        source.setName("Somewhere");
        source.setType("Planet");
        source.setDimension("");

        LocationSnapshot snapshot = mapper.toSnapshot(source);

        assertThat(snapshot.getDimension()).isNull();
        assertThat(snapshot.getType()).isEqualTo("Planet");
    }

    private static ExternalCharacter minimalCharacter() {
        ExternalCharacter source = new ExternalCharacter();
        source.setId(2L);
        source.setName("Morty");
        source.setStatus("Alive");
        source.setGender("Male");
        return source;
    }

    private static ExternalReference reference(String name, String url) {
        ExternalReference reference = new ExternalReference();
        reference.setName(name);
        reference.setUrl(url);
        return reference;
    }
}
