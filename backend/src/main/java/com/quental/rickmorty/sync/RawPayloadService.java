package com.quental.rickmorty.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quental.rickmorty.config.TopicProperties;
import com.quental.rickmorty.outbox.OutboxEventEntity;
import com.quental.rickmorty.outbox.OutboxEventRepository;
import com.quental.rickmorty.sync.domain.RawPayloadEntity;
import com.quental.rickmorty.sync.domain.SyncRunEntity;
import com.quental.rickmorty.sync.external.ResourceType;
import com.quental.rickmorty.sync.messaging.ResourceMessage;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RawPayloadService {

    private final RawPayloadRepository rawPayloadRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final TopicProperties topics;

    public RawPayloadService(RawPayloadRepository rawPayloadRepository, OutboxEventRepository outboxEventRepository,
                             ObjectMapper objectMapper, TopicProperties topics) {
        this.rawPayloadRepository = rawPayloadRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.topics = topics;
    }

    @Transactional
    public boolean queue(SyncRunEntity run, ResourceType type, JsonNode payload) {
        try {
            long externalId = payload.path("id").asLong();
            String raw = objectMapper.writeValueAsString(payload);
            String contentHash = sha256(raw);
            if (rawPayloadRepository.existsByResourceTypeAndExternalIdAndContentHash(type, externalId, contentHash)) {
                return false;
            }
            rawPayloadRepository.save(new RawPayloadEntity(run, type, externalId, contentHash, raw));
            String messageId = UUID.randomUUID().toString();
            ResourceMessage message = new ResourceMessage(messageId, run.getId(), type, externalId, payload);
            String envelope = objectMapper.writeValueAsString(message);
            outboxEventRepository.save(new OutboxEventEntity(type.name(), String.valueOf(externalId),
                    "RAW_RESOURCE_AVAILABLE", topics.getRaw(), envelope));
            return true;
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize external payload", exception);
        }
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(digest.length * 2);
            for (byte part : digest) {
                hash.append(String.format("%02x", part & 0xff));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
