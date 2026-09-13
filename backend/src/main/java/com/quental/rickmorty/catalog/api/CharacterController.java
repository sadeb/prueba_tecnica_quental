package com.quental.rickmorty.catalog.api;

import com.quental.rickmorty.catalog.CharacterQueryService;
import com.quental.rickmorty.shared.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/characters")
@SecurityRequirement(name = "opaqueBearer")
public class CharacterController {

    private final CharacterQueryService service;

    public CharacterController(CharacterQueryService service) {
        this.service = service;
    }

    @Operation(summary = "List synchronized characters with optional filters")
    @GetMapping
    public PageResponse<CharacterSummaryResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.search(name, status, species, type, gender, page, size);
    }

    @Operation(summary = "Get character detail from PostgreSQL")
    @GetMapping("/{characterId}")
    public CharacterDetailResponse detail(@PathVariable @Min(1) Long characterId) {
        return service.detail(characterId);
    }

    @Operation(summary = "Get related characters using shared episodes in Neo4j")
    @GetMapping("/{characterId}/related")
    public List<RelatedCharacterResponse> related(@PathVariable @Min(1) Long characterId,
                                                   @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        return service.related(characterId, limit);
    }
}
