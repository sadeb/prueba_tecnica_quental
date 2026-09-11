package com.quental.rickmorty.sync;

import com.quental.rickmorty.sync.domain.RawPayloadEntity;
import com.quental.rickmorty.sync.external.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawPayloadRepository extends JpaRepository<RawPayloadEntity, String> {
    boolean existsByResourceTypeAndExternalIdAndContentHash(ResourceType resourceType, Long externalId, String contentHash);
}
