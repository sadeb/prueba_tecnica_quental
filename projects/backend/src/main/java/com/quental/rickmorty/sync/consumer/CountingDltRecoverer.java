package com.quental.rickmorty.sync.consumer;

import com.quental.rickmorty.sync.SyncRunService;
import com.quental.rickmorty.sync.message.SyncMessageCodec;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerAwareRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

import java.util.Optional;

/**
 * Wraps the DeadLetterPublishingRecoverer: after the record is published to <topic>.DLT it logs the
 * trace demanded by spec/03 (topic, key, cause) and increments sync_runs.failed_messages (ADR-006).
 * If publishing to the DLT itself fails, the exception propagates and the error handler retries the record.
 */
public class CountingDltRecoverer implements ConsumerAwareRecordRecoverer {

    private static final Logger log = LoggerFactory.getLogger(CountingDltRecoverer.class);

    private final DeadLetterPublishingRecoverer delegate;
    private final SyncRunService syncRunService;
    private final SyncMessageCodec codec;

    public CountingDltRecoverer(DeadLetterPublishingRecoverer delegate, SyncRunService syncRunService, SyncMessageCodec codec) {
        this.delegate = delegate;
        this.syncRunService = syncRunService;
        this.codec = codec;
    }

    @Override
    public void accept(ConsumerRecord<?, ?> consumerRecord, Consumer<?, ?> consumer, Exception exception) {
        delegate.accept(consumerRecord, consumer, exception);
        Throwable cause = rootCause(exception);
        log.error("Message sent to DLT: topic={} partition={} offset={} key={} cause={}: {}",
                consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), consumerRecord.key(),
                cause.getClass().getSimpleName(), cause.getMessage());
        try {
            Object value = consumerRecord.value();
            Optional<Long> runId = codec.tryExtractRunId(value == null ? null : value.toString());
            syncRunService.recordFailedMessage(runId);
        } catch (RuntimeException ex) {
            // Never let counter bookkeeping re-trigger the DLT publish.
            log.error("Could not record failed message counter: {}", ex.getMessage());
        }
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
