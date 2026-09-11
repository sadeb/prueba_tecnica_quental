package com.quental.rickmorty.sync;

import com.quental.rickmorty.shared.NotFoundException;
import com.quental.rickmorty.sync.domain.SyncRunEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncService {

    private final SyncRunRepository repository;
    private final SyncWorker worker;

    public SyncService(SyncRunRepository repository, SyncWorker worker) {
        this.repository = repository;
        this.worker = worker;
    }

    public SyncRunEntity start() {
        // Persist and commit the run before handing it to the asynchronous worker.
        // Otherwise the worker may start before the surrounding transaction commits.
        SyncRunEntity run = repository.saveAndFlush(SyncRunEntity.start());
        worker.execute(run.getId());
        return run;
    }

    @Transactional(readOnly = true)
    public SyncRunEntity get(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Sync run not found"));
    }

    @Transactional(readOnly = true)
    public Page<SyncRunEntity> list(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
