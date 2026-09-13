package com.quental.rickmorty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.quental.rickmorty.sync.message.CharacterSnapshot;
import com.quental.rickmorty.sync.message.EpisodeSnapshot;
import com.quental.rickmorty.sync.message.LocationSnapshot;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.util.List;

/** Small explicit factories shared by tests; override what a test cares about with the overloads. */
public final class TestData {

    private TestData() {
    }

    /** Same relevant defaults as Boot's ObjectMapper: java.time module and ISO-8601 dates (not timestamps). */
    public static ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    public static CharacterSnapshot aCharacterSnapshot(long externalId) {
        return aCharacterSnapshot(externalId, 1L, 20L, List.of(1L, 2L));
    }

    public static CharacterSnapshot aCharacterSnapshot(long externalId, Long originId, Long locationId, List<Long> episodeIds) {
        return new CharacterSnapshot(externalId, "Character " + externalId, "ALIVE", "Human", null, "MALE",
                "https://img/" + externalId + ".jpeg", originId, originId == null ? null : "Origin " + originId,
                locationId, locationId == null ? null : "Location " + locationId, episodeIds);
    }

    public static EpisodeSnapshot anEpisodeSnapshot(long externalId) {
        return new EpisodeSnapshot(externalId, "Episode " + externalId, "December 2, 2013",
                String.format("S01E%02d", externalId), List.of(1L, 2L));
    }

    public static LocationSnapshot aLocationSnapshot(long externalId) {
        return new LocationSnapshot(externalId, "Location " + externalId, "Planet", "Dimension C-137");
    }

    public static String anExternalCharacterJson(long id, String originUrl, String... episodeUrls) {
        StringBuilder episodes = new StringBuilder();
        for (int i = 0; i < episodeUrls.length; i++) {
            episodes.append(i == 0 ? "" : ",").append('"').append(episodeUrls[i]).append('"');
        }
        return "{\"id\":" + id + ",\"name\":\"Rick Sanchez\",\"status\":\"Alive\",\"species\":\"Human\",\"type\":\"\","
                + "\"gender\":\"Male\",\"origin\":{\"name\":\"Earth (C-137)\",\"url\":\"" + originUrl + "\"},"
                + "\"location\":{\"name\":\"Citadel of Ricks\",\"url\":\"https://rickandmortyapi.com/api/location/3\"},"
                + "\"image\":\"https://rickandmortyapi.com/api/character/avatar/" + id + ".jpeg\","
                + "\"episode\":[" + episodes + "],\"url\":\"https://rickandmortyapi.com/api/character/" + id + "\","
                + "\"created\":\"2017-11-04T18:48:46.250Z\"}";
    }

    public static String aCharacterPageJson(Integer pages, String next, String... elements) {
        return "{\"info\":{\"count\":826,\"pages\":" + pages + ",\"next\":" + (next == null ? "null" : "\"" + next + "\"")
                + ",\"prev\":null},\"results\":[" + String.join(",", elements) + "]}";
    }
}
