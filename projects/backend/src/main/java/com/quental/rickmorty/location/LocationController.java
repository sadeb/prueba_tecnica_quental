package com.quental.rickmorty.location;

import com.quental.rickmorty.common.ApiError;
import com.quental.rickmorty.common.PageResponse;
import com.quental.rickmorty.location.dto.LocationDetailResponse;
import com.quental.rickmorty.location.dto.LocationSummaryResponse;
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
@RequestMapping("/api/locations")
@Validated
@Tag(name = "Locations", description = "Synchronised locations (public)")
public class LocationController {

    private final LocationQueryService queryService;

    public LocationController(LocationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "List locations (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of locations")
    @ApiResponse(responseCode = "400", description = "Invalid pagination", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public PageResponse<LocationSummaryResponse> list(
            @Parameter(description = "Page number, 0-based") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size, 1..100") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return queryService.list(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Location detail with its residents")
    @ApiResponse(responseCode = "200", description = "Location found")
    @ApiResponse(responseCode = "404", description = "Location not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public LocationDetailResponse detail(@Parameter(description = "Internal id") @PathVariable long id) {
        return queryService.getDetail(id);
    }
}
