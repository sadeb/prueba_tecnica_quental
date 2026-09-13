package com.quental.rickmorty.character;

import com.quental.rickmorty.character.dto.CharacterDetailResponse;
import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.character.dto.RelatedCharacterResponse;
import com.quental.rickmorty.common.ApiError;
import com.quental.rickmorty.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/characters")
@Validated
@Tag(name = "Characters", description = "Synchronised characters (public)")
public class CharacterController {

    private final CharacterQueryService queryService;

    public CharacterController(CharacterQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "Search characters with filters and pagination",
            description = "name: partial, case-insensitive. species: exact, case-insensitive. status/gender: enum values, case-insensitive.")
    @ApiResponse(responseCode = "200", description = "Page of characters (empty content past the last page)")
    @ApiResponse(responseCode = "400", description = "Invalid filter or pagination", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public PageResponse<CharacterSummaryResponse> search(
            @Parameter(description = "Partial name, case-insensitive") @RequestParam(required = false) String name,
            @Parameter(description = "ALIVE, DEAD or UNKNOWN") @RequestParam(required = false) CharacterStatus status,
            @Parameter(description = "Exact species, case-insensitive") @RequestParam(required = false) String species,
            @Parameter(description = "FEMALE, MALE, GENDERLESS or UNKNOWN") @RequestParam(required = false) CharacterGender gender,
            @Parameter(description = "Page number, 0-based") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size, 1..100") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return queryService.search(new CharacterFilter(name, status, species, gender), page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Character detail with origin, location and episodes")
    @ApiResponse(responseCode = "200", description = "Character found")
    @ApiResponse(responseCode = "404", description = "Character not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public CharacterDetailResponse detail(@Parameter(description = "Internal id") @PathVariable long id) {
        return queryService.getDetail(id);
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Characters related by shared episodes (Neo4j)",
            description = "Ordered by number of shared episodes, descending. Resolved on the graph store.")
    @ApiResponse(responseCode = "200", description = "Related characters (may be empty)")
    @ApiResponse(responseCode = "400", description = "Invalid limit", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Character not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "503", description = "Graph store unavailable", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<RelatedCharacterResponse> related(
            @Parameter(description = "Internal id") @PathVariable long id,
            @Parameter(description = "Maximum results, 1..100") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        return queryService.getRelated(id, limit);
    }
}
