package com.quental.rickmorty.sync;

import com.quental.rickmorty.auth.TokenService;
import com.quental.rickmorty.common.ConflictException;
import com.quental.rickmorty.common.NotFoundException;
import com.quental.rickmorty.config.SecurityConfig;
import com.quental.rickmorty.sync.producer.SyncProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SyncAdminController.class)
@Import({SecurityConfig.class, TokenService.class})
@ActiveProfiles("test")
class SyncAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TokenService tokenService;
    @MockBean
    private SyncProducerService producerService;
    @MockBean
    private SyncRunService runService;

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(post("/api/admin/sync"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void shouldReturn202WithRunIdWhenLaunched() throws Exception {
        when(producerService.launch()).thenReturn(run(5L));

        mockMvc.perform(post("/api/admin/sync").header("Authorization", "Bearer " + bearer()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.runId").value(5))
                .andExpect(jsonPath("$.startedAt").isNotEmpty());
    }

    @Test
    void shouldReturn409WhenARunIsAlreadyRunning() throws Exception {
        when(producerService.launch()).thenThrow(new ConflictException("A synchronisation is already running"));

        mockMvc.perform(post("/api/admin/sync").header("Authorization", "Bearer " + bearer()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void shouldReturnRunStateAndCounters() throws Exception {
        SyncRun run = run(5L);
        run.finish(SyncRunStatus.PARTIAL, 900L, 2L, 1L, Instant.now());
        when(runService.get(5L)).thenReturn(run);

        mockMvc.perform(get("/api/admin/sync/5").header("Authorization", "Bearer " + bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIAL"))
                .andExpect(jsonPath("$.publishedMessages").value(900))
                .andExpect(jsonPath("$.failedPages").value(1));
    }

    @Test
    void shouldReturn404ForUnknownRun() throws Exception {
        when(runService.get(99L)).thenThrow(NotFoundException.of("Sync run", 99L));

        mockMvc.perform(get("/api/admin/sync/99").header("Authorization", "Bearer " + bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    private String bearer() {
        return tokenService.issue(1L, "rick").getToken();
    }

    private static SyncRun run(long id) {
        SyncRun run = SyncRun.started(Instant.parse("2026-09-13T10:00:00Z"));
        ReflectionTestUtils.setField(run, "id", id);
        return run;
    }
}
