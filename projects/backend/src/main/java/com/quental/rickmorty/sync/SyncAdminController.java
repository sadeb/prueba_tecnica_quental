package com.quental.rickmorty.sync;

import com.quental.rickmorty.common.ApiError;
import com.quental.rickmorty.config.OpenApiConfig;
import com.quental.rickmorty.sync.dto.SyncRunResponse;
import com.quental.rickmorty.sync.dto.SyncStartResponse;
import com.quental.rickmorty.sync.producer.SyncProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sync")
@Tag(name = "Admin", description = "Synchronisation with the external source. Requires the system administrator (role ADMIN, ADR-012).")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class SyncAdminController {

    private final SyncProducerService producerService;
    private final SyncRunService runService;

    public SyncAdminController(SyncProducerService producerService, SyncRunService runService) {
        this.producerService = producerService;
        this.runService = runService;
    }

    @PostMapping
    @Operation(summary = "Launch a full synchronisation",
            description = "Downloads locations, episodes and characters from the source and publishes them to Kafka. "
                    + "Asynchronous: returns 202 with the run id; poll GET /api/admin/sync/{runId}. Idempotent end to end (ADR-002).")
    @ApiResponse(responseCode = "202", description = "Run accepted")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Token without role ADMIN", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "A run is already in progress", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<SyncStartResponse> launch() {
        SyncRun run = producerService.launch();
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new SyncStartResponse(run.getId(), run.getStartedAt()));
    }

    @GetMapping("/{runId}")
    @Operation(summary = "State and counters of a synchronisation run")
    @ApiResponse(responseCode = "200", description = "Run found")
    @ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "403", description = "Token without role ADMIN", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Unknown run", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public SyncRunResponse get(@PathVariable Long runId) {
        return SyncRunResponse.from(runService.get(runId));
    }
}
