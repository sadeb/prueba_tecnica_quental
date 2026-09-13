package com.quental.rickmorty.sync;

import com.quental.rickmorty.sync.domain.SyncRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncRunRepository extends JpaRepository<SyncRunEntity, String> {
}
