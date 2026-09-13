package com.quental.rickmorty.user;

import com.quental.rickmorty.auth.CurrentUser;
import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.common.ApiError;
import com.quental.rickmorty.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/favorites")
@Tag(name = "Favorites", description = "Favorite characters of the authenticated user")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    @Operation(summary = "List my favorite characters (oldest first)")
    @ApiResponse(responseCode = "200", description = "Favorites")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public List<CharacterSummaryResponse> list() {
        return favoriteService.list(CurrentUser.get().getId());
    }

    @PostMapping("/{characterId}")
    @Operation(summary = "Add a character to my favorites",
            description = "Idempotent: 201 when created, 200 when it was already a favorite.")
    @ApiResponse(responseCode = "201", description = "Favorite created")
    @ApiResponse(responseCode = "200", description = "Already a favorite")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Character not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<CharacterSummaryResponse> add(
            @Parameter(description = "Internal character id") @PathVariable long characterId) {
        FavoriteService.FavoriteAddResult result = favoriteService.add(CurrentUser.get().getId(), characterId);
        HttpStatus status = result.isCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.getCharacter());
    }

    @DeleteMapping("/{characterId}")
    @Operation(summary = "Remove a character from my favorites")
    @ApiResponse(responseCode = "204", description = "Favorite removed")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Not a favorite", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Void> remove(@Parameter(description = "Internal character id") @PathVariable long characterId) {
        favoriteService.remove(CurrentUser.get().getId(), characterId);
        return ResponseEntity.noContent().build();
    }
}
