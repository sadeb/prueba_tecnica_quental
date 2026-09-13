package com.quental.rickmorty.config;

import com.quental.rickmorty.sync.SyncProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * One topic per entity plus its dead-letter topic, 1 partition / RF 1 (ADR-003, ADR-006).
 * Created by KafkaAdmin at startup; the broker has auto.create.topics.enable=false.
 */
@Configuration
@EnableConfigurationProperties(SyncProperties.class)
public class KafkaTopicsConfig {

    @Bean
    public KafkaAdmin.NewTopics syncTopics(SyncProperties properties) {
        SyncProperties.Topics topics = properties.getTopics();
        return new KafkaAdmin.NewTopics(
                topic(topics.getLocations()), topic(properties.dltOf(topics.getLocations())),
                topic(topics.getEpisodes()), topic(properties.dltOf(topics.getEpisodes())),
                topic(topics.getCharacters()), topic(properties.dltOf(topics.getCharacters())));
    }

    private static org.apache.kafka.clients.admin.NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(1).replicas(1).build();
    }
}
