package com.quental.rickmorty.episode;

import com.quental.rickmorty.common.ApiError;
import com.quental.rickmorty.common.PageResponse;
import com.quental.rickmorty.episode.dto.EpisodeDetailResponse;
import com.quental.rickmorty.episode.dto.EpisodeSummaryResponse;
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

@RestController
@RequestMapping("/api/episodes")
@Validated
@Tag(name = "Episodes", description = "Synchronised episodes (public)")
public class EpisodeController {

    private final EpisodeQueryService queryService;

    public EpisodeController(EpisodeQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "List episodes (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of episodes")
    @ApiResponse(responseCode = "400", description = "Invalid pagination", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public PageResponse<EpisodeSummaryResponse> list(
            @Parameter(description = "Page number, 0-based") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size, 1..100") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return queryService.list(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Episode detail with its characters")
    @ApiResponse(responseCode = "200", description = "Episode found")
    @ApiResponse(responseCode = "404", description = "Episode not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public EpisodeDetailResponse detail(@Parameter(description = "Internal id") @PathVariable long id) {
        return queryService.getDetail(id);
    }
}
