package com.quental.rickmorty.sync;

import com.quental.rickmorty.sync.domain.ProcessedMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessageEntity, String> {
}
