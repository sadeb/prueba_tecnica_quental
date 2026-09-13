package com.quental.rickmorty.sync;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SyncRunJpaRepository extends JpaRepository<SyncRun, Long> {

    boolean existsByStatus(SyncRunStatus status);

    List<SyncRun> findByStatus(SyncRunStatus status);

    Optional<SyncRun> findFirstByOrderByStartedAtDesc();

    /** Atomic increment: the consumer thread and the producer thread may touch the same row. */
    @Modifying
    @Query("update SyncRun r set r.failedMessages = r.failedMessages + 1 where r.id = :id")
    int incrementFailedMessages(@Param("id") Long id);
}
