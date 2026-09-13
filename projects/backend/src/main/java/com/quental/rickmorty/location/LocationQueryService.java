package com.quental.rickmorty.location;

import com.quental.rickmorty.character.CharacterJpaRepository;
import com.quental.rickmorty.character.dto.CharacterDtoMapper;
import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.common.NotFoundException;
import com.quental.rickmorty.common.PageResponse;
import com.quental.rickmorty.location.dto.LocationDetailResponse;
import com.quental.rickmorty.location.dto.LocationDtoMapper;
import com.quental.rickmorty.location.dto.LocationSummaryResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationQueryService {

    private final LocationJpaRepository locations;
    private final CharacterJpaRepository characters;

    public LocationQueryService(LocationJpaRepository locations, CharacterJpaRepository characters) {
        this.locations = locations;
        this.characters = characters;
    }

    @Transactional(readOnly = true)
    public PageResponse<LocationSummaryResponse> list(int page, int size) {
        return PageResponse.from(locations.findByPlaceholderFalse(PageRequest.of(page, size, Sort.by("id")))
                .map(LocationDtoMapper::toSummary));
    }

    /** Residents = inverse of the character's current location (spec/04). Placeholder rows are not exposed. */
    @Transactional(readOnly = true)
    public LocationDetailResponse getDetail(long id) {
        Location location = locations.findByIdAndPlaceholderFalse(id)
                .orElseThrow(() -> NotFoundException.of("Location", id));
        List<CharacterSummaryResponse> residents = characters.findByLocationIdAndPlaceholderFalseOrderByNameAsc(id).stream()
                .map(CharacterDtoMapper::toSummary)
                .collect(Collectors.toList());
        return LocationDtoMapper.toDetail(location, residents);
    }
}
