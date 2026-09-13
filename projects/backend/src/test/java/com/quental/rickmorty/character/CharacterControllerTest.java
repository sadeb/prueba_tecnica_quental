package com.quental.rickmorty.character;

import com.quental.rickmorty.auth.TokenService;
import com.quental.rickmorty.character.dto.CharacterSummaryResponse;
import com.quental.rickmorty.common.NotFoundException;
import com.quental.rickmorty.common.PageResponse;
import com.quental.rickmorty.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CharacterController.class)
@Import({SecurityConfig.class, TokenService.class})
@ActiveProfiles("test")
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CharacterQueryService queryService;

    @Test
    void shouldReturn400WithFieldDetailWhenSizeExceeds100() throws Exception {
        mockMvc.perform(get("/api/characters").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("size"));
        verify(queryService, never()).search(any(), anyInt(), anyInt());
    }

    @Test
    void shouldReturn400WhenStatusIsNotAnEnumValue() throws Exception {
        mockMvc.perform(get("/api/characters").param("status", "banana"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("status"));
    }

    @Test
    void shouldAcceptLowercaseEnumFiltersAndPassThemToTheService() throws Exception {
        when(queryService.search(any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(summary(1L)), 0, 20, 1, 1));

        mockMvc.perform(get("/api/characters").param("status", "alive").param("gender", "Male").param("name", "rick"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Rick Sanchez"))
                .andExpect(jsonPath("$.totalElements").value(1));

        ArgumentCaptor<CharacterFilter> filter = ArgumentCaptor.forClass(CharacterFilter.class);
        verify(queryService).search(filter.capture(), eq(0), eq(20));
        assertThat(filter.getValue().getStatus()).isEqualTo(CharacterStatus.ALIVE);
        assertThat(filter.getValue().getGender()).isEqualTo(CharacterGender.MALE);
        assertThat(filter.getValue().getName()).isEqualTo("rick");
    }

    @Test
    void shouldReturn200WithEmptyContentPastTheLastPage() throws Exception {
        when(queryService.search(any(), eq(500), eq(20))).thenReturn(new PageResponse<>(List.of(), 500, 20, 826, 42));

        mockMvc.perform(get("/api/characters").param("page", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalPages").value(42));
    }

    @Test
    void shouldReturn404ApiErrorWhenCharacterDoesNotExist() throws Exception {
        when(queryService.getDetail(999L)).thenThrow(NotFoundException.of("Character", 999L));

        mockMvc.perform(get("/api/characters/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Character 999 not found"))
                .andExpect(jsonPath("$.path").value("/api/characters/999"));
    }

    @Test
    void shouldReturn400WhenRelatedLimitIsZero() throws Exception {
        mockMvc.perform(get("/api/characters/1/related").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("limit"));
    }

    @Test
    void shouldReturn404ApiErrorForUnmappedRoute() throws Exception {
        mockMvc.perform(get("/api/nothing-here"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void shouldReturn405ApiErrorForUnsupportedMethod() throws Exception {
        mockMvc.perform(post("/api/characters"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void shouldReturn400WhenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/characters/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn503ApiErrorWhenGraphStoreIsUnavailable() throws Exception {
        when(queryService.getRelated(1L, 10)).thenThrow(new ServiceUnavailableException("neo4j down"));

        mockMvc.perform(get("/api/characters/1/related"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("SERVICE_UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("A backing service is not available"));
    }

    @Test
    void shouldReturn409ApiErrorOnIntegrityViolation() throws Exception {
        when(queryService.getDetail(1L)).thenThrow(new DataIntegrityViolationException("duplicate"));

        mockMvc.perform(get("/api/characters/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void shouldReturn500GenericApiErrorWithoutInternalDetails() throws Exception {
        when(queryService.getDetail(1L)).thenThrow(new IllegalStateException("secret internal detail"));

        mockMvc.perform(get("/api/characters/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Unexpected error"));
    }

    private static CharacterSummaryResponse summary(long id) {
        return new CharacterSummaryResponse(id, id, "Rick Sanchez", CharacterStatus.ALIVE, "Human", null,
                CharacterGender.MALE, null);
    }
}
