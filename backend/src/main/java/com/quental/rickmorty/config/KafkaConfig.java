package com.quental.rickmorty.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> template, TopicProperties topics) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, exception) -> new TopicPartition(record.topic() + topics.getDeadLetterSuffix(), record.partition()));
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1_000L, 3L));
    }
}
