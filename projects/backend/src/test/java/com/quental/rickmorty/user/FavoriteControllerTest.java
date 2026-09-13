package com.quental.rickmorty.user;

import com.quental.rickmorty.auth.TokenService;
import com.quental.rickmorty.character.CharacterGender;
import com.quental.rickmorty.character.CharacterStatus;
import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.common.NotFoundException;
import com.quental.rickmorty.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Also covers BearerTokenFilter and the entry point: real TokenService with the test secret. */
@WebMvcTest(controllers = FavoriteController.class)
@Import({SecurityConfig.class, TokenService.class})
@ActiveProfiles("test")
class FavoriteControllerTest {

    private static final long USER_ID = 42L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokenService;
    @MockBean
    private FavoriteService favoriteService;

    @Test
    void shouldReturn401ApiErrorWhenNoToken() throws Exception {
        mockMvc.perform(get("/api/users/me/favorites"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.path").value("/api/users/me/favorites"));
    }

    @Test
    void shouldReturn401WhenTokenIsTampered() throws Exception {
        String token = bearer();
        String tampered = token.substring(0, token.length() - 3) + "xyz";

        mockMvc.perform(get("/api/users/me/favorites").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void shouldListFavoritesOfTheAuthenticatedUser() throws Exception {
        when(favoriteService.list(USER_ID)).thenReturn(List.of(summary(1L)));

        mockMvc.perform(get("/api/users/me/favorites").header("Authorization", "Bearer " + bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Rick Sanchez"));
        verify(favoriteService).list(USER_ID);
    }

    @Test
    void shouldReturn201WhenFavoriteIsCreated() throws Exception {
        when(favoriteService.add(USER_ID, 1L)).thenReturn(new FavoriteService.FavoriteAddResult(summary(1L), true));

        mockMvc.perform(post("/api/users/me/favorites/1").header("Authorization", "Bearer " + bearer()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").value(1));
    }

    @Test
    void shouldReturn200WhenFavoriteAlreadyExisted() throws Exception {
        when(favoriteService.add(USER_ID, 1L)).thenReturn(new FavoriteService.FavoriteAddResult(summary(1L), false));

        mockMvc.perform(post("/api/users/me/favorites/1").header("Authorization", "Bearer " + bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId").value(1));
    }

    @Test
    void shouldReturn404WhenCharacterDoesNotExist() throws Exception {
        when(favoriteService.add(USER_ID, 999L)).thenThrow(NotFoundException.of("Character", 999L));

        mockMvc.perform(post("/api/users/me/favorites/999").header("Authorization", "Bearer " + bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Character 999 not found"));
    }

    @Test
    void shouldReturn204WhenFavoriteRemoved() throws Exception {
        mockMvc.perform(delete("/api/users/me/favorites/1").header("Authorization", "Bearer " + bearer()))
                .andExpect(status().isNoContent());
        verify(favoriteService).remove(eq(USER_ID), eq(1L));
    }

    @Test
    void shouldReturn404WhenRemovingSomethingThatIsNotAFavorite() throws Exception {
        doThrow(new NotFoundException("Character 2 is not a favorite")).when(favoriteService).remove(USER_ID, 2L);

        mockMvc.perform(delete("/api/users/me/favorites/2").header("Authorization", "Bearer " + bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    private String bearer() {
        return tokenService.issue(USER_ID, "rick").getToken();
    }

    private static CharacterSummaryResponse summary(long id) {
        return new CharacterSummaryResponse(id, id, "Rick Sanchez", CharacterStatus.ALIVE, "Human", null,
                CharacterGender.MALE, null);
    }
}
