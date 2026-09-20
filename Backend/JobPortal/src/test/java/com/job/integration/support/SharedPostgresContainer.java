package com.job.integration.support;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * One Postgres container for the whole test JVM (the Testcontainers "singleton container"
 * pattern), so every integration test class that shares the same Spring context configuration
 * also shares the same database instead of paying to start a fresh container per class. There is
 * deliberately no stop() call anywhere: Testcontainers' Ryuk reaper removes the container when
 * the JVM exits.
 */
public final class SharedPostgresContainer extends PostgreSQLContainer<SharedPostgresContainer> {

    private static final String IMAGE = "postgres:16-alpine";
    private static SharedPostgresContainer instance;

    private SharedPostgresContainer() {
        super(DockerImageName.parse(IMAGE));
    }

    public static synchronized SharedPostgresContainer getInstance() {
        if (instance == null) {
            instance = new SharedPostgresContainer();
            instance.start();
        }
        return instance;
    }
}
