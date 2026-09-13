package com.quental.rickmorty.sync.producer;

import com.quental.rickmorty.sync.message.SyncMessage;
import com.quental.rickmorty.sync.message.SyncMessageCodec;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publishes one snapshot per message, key = externalId (ADR-003: same entity -> same partition, in order).
 * Waits for the broker ack so counters are exact and a broker outage is detected page by page.
 */
@Component
public class SyncPublisher {

    private static final long SEND_TIMEOUT_SECONDS = 30;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SyncMessageCodec codec;

    public SyncPublisher(KafkaTemplate<String, String> kafkaTemplate, SyncMessageCodec codec) {
        this.kafkaTemplate = kafkaTemplate;
        this.codec = codec;
    }

    public void publish(String topic, SyncMessage message) {
        String key = String.valueOf(message.getExternalId());
        try {
            kafkaTemplate.send(topic, key, codec.encode(message)).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new SyncPublishException("Interrupted while publishing to " + topic, ex);
        } catch (ExecutionException | TimeoutException ex) {
            throw new SyncPublishException("Could not publish key " + key + " to " + topic, ex);
        }
    }
}
