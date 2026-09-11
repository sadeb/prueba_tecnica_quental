package com.quental.rickmorty.favorites;

import com.quental.rickmorty.auth.AuthenticatedUser;
import com.quental.rickmorty.catalog.api.CharacterSummaryResponse;
import com.quental.rickmorty.shared.PageResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/users/me/favorites")
public class FavoriteController {

    private final FavoriteService service;

    public FavoriteController(FavoriteService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<CharacterSummaryResponse> list(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @RequestParam(defaultValue = "0") @Min(0) int page,
                                                        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(user.getId(), page, size);
    }

    @PutMapping("/{characterId}")
    public CharacterSummaryResponse add(@AuthenticationPrincipal AuthenticatedUser user,
                                         @PathVariable @Min(1) Long characterId) {
        return service.add(user.getId(), characterId);
    }

    @DeleteMapping("/{characterId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal AuthenticatedUser user,
                       @PathVariable @Min(1) Long characterId) {
        service.remove(user.getId(), characterId);
    }
}
