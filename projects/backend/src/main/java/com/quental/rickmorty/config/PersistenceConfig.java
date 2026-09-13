package com.quental.rickmorty.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

/**
 * With JPA and Neo4j both on the classpath, Boot auto-configures only one "transactionManager"
 * (both are conditional on a missing TransactionManager, and Neo4j's is processed first).
 * The JPA one is declared explicitly and marked primary so @Transactional services use it.
 * Neo4j writes run in auto-commit through Neo4jClient (ADR-004: no distributed transaction).
 */
@Configuration
public class PersistenceConfig {

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
