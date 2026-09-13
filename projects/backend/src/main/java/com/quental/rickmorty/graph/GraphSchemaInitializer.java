package com.quental.rickmorty.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Creates the unique constraints on externalId at startup (idempotent, IF NOT EXISTS).
 * A Neo4j outage at boot is logged, not fatal: the API still serves PostgreSQL data and the
 * health endpoint reports the graph as DOWN; the constraints are re-attempted on next start.
 */
@Component
public class GraphSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(GraphSchemaInitializer.class);

    private final GraphRepository graphRepository;

    public GraphSchemaInitializer(GraphRepository graphRepository) {
        this.graphRepository = graphRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            graphRepository.ensureConstraints();
            log.info("Neo4j constraints ensured");
        } catch (RuntimeException ex) {
            log.error("Could not ensure Neo4j constraints (graph unavailable?): {}", ex.getMessage());
        }
    }
}
