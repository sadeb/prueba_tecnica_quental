package com.quental.rickmorty.external;

import com.quental.rickmorty.character.CharacterGender;
import com.quental.rickmorty.character.CharacterStatus;
import com.quental.rickmorty.sync.message.CharacterSnapshot;
import com.quental.rickmorty.sync.message.EpisodeSnapshot;
import com.quental.rickmorty.sync.message.LocationSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * External DTO -> internal snapshot. Only place that knows the provider format (spec/02 point 3).
 * Normalisation rules from ADR-001: "" -> null, reference without url -> null id, relation urls -> ids.
 */
@Component
public class ExternalSnapshotMapper {

    private static final Logger log = LoggerFactory.getLogger(ExternalSnapshotMapper.class);

    public CharacterSnapshot toSnapshot(ExternalCharacter source) {
        CharacterStatus status = CharacterStatus.fromExternal(source.getStatus());
        CharacterGender gender = CharacterGender.fromExternal(source.getGender());
        warnIfUnrecognised("status", source.getStatus(), status == CharacterStatus.UNKNOWN, source.getId());
        warnIfUnrecognised("gender", source.getGender(), gender == CharacterGender.UNKNOWN, source.getId());
        Long originId = source.getOrigin() == null ? null : ExternalIdParser.fromUrl(source.getOrigin().getUrl());
        Long locationId = source.getLocation() == null ? null : ExternalIdParser.fromUrl(source.getLocation().getUrl());
        return new CharacterSnapshot(
                source.getId(),
                blankToNull(source.getName()),
                status.name(),
                blankToNull(source.getSpecies()),
                blankToNull(source.getType()),
                gender.name(),
                blankToNull(source.getImage()),
                originId,
                originId == null ? null : blankToNull(source.getOrigin().getName()),
                locationId,
                locationId == null ? null : blankToNull(source.getLocation().getName()),
                toIds(source.getEpisode()));
    }

    public EpisodeSnapshot toSnapshot(ExternalEpisode source) {
        return new EpisodeSnapshot(
                source.getId(),
                blankToNull(source.getName()),
                blankToNull(source.getAirDate()),
                blankToNull(source.getEpisode()),
                toIds(source.getCharacters()));
    }

    public LocationSnapshot toSnapshot(ExternalLocation source) {
        return new LocationSnapshot(
                source.getId(),
                blankToNull(source.getName()),
                blankToNull(source.getType()),
                blankToNull(source.getDimension()));
    }

    static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static List<Long> toIds(List<String> urls) {
        List<Long> ids = new ArrayList<>();
        if (urls == null) {
            return ids;
        }
        for (String url : urls) {
            Long id = ExternalIdParser.fromUrl(url);
            if (id != null && !ids.contains(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static void warnIfUnrecognised(String field, String raw, boolean unknown, Long id) {
        if (unknown && raw != null && !raw.isBlank() && !"unknown".equalsIgnoreCase(raw.trim())) {
            log.warn("Unrecognised {} '{}' for character {}, stored as UNKNOWN", field, raw, id);
        }
    }
}
