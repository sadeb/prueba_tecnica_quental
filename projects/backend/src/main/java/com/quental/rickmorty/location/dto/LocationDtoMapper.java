package com.quental.rickmorty.location.dto;

import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.location.Location;

import java.util.List;

public final class LocationDtoMapper {

    private LocationDtoMapper() {
    }

    public static LocationSummaryResponse toSummary(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationSummaryResponse(location.getId(), location.getExternalId(), location.getName(),
                location.getType(), location.getDimension());
    }

    public static LocationDetailResponse toDetail(Location location, List<CharacterSummaryResponse> residents) {
        return new LocationDetailResponse(location.getId(), location.getExternalId(), location.getName(),
                location.getType(), location.getDimension(), residents);
    }
}
