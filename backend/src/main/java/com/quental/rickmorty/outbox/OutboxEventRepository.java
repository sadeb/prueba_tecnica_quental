package com.quental.rickmorty.outbox;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, String> {

    @Query("select e from OutboxEventEntity e where e.status in (com.quental.rickmorty.outbox.OutboxStatus.PENDING, com.quental.rickmorty.outbox.OutboxStatus.FAILED) and e.attempts < 5 order by e.createdAt")
    List<OutboxEventEntity> findPublishable(Pageable pageable);
}
