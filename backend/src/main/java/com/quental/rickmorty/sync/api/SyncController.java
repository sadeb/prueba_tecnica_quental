package com.quental.rickmorty.sync.api;

import com.quental.rickmorty.shared.PageResponse;
import com.quental.rickmorty.sync.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/sync-runs")
@SecurityRequirement(name = "opaqueBearer")
public class SyncController {

    private final SyncService service;

    public SyncController(SyncService service) {
        this.service = service;
    }

    @Operation(summary = "Start a full asynchronous synchronization")
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SyncRunResponse start() {
        return new SyncRunResponse(service.start());
    }

    @GetMapping("/{syncRunId}")
    public SyncRunResponse get(@PathVariable String syncRunId) {
        return new SyncRunResponse(service.get(syncRunId));
    }

    @GetMapping
    public PageResponse<SyncRunResponse> list(@RequestParam(defaultValue = "0") @Min(0) int page,
                                              @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.from(service.list(PageRequest.of(page, size, Sort.by("startedAt").descending())), SyncRunResponse::new);
    }
}
