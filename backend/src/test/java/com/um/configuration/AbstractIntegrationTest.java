package com.um.configuration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;


@Testcontainers
public abstract class AbstractIntegrationTest {
	
	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine")
			.withDatabaseName("taskmanager_db")
			.withUsername("tmuser")
			.withPassword("tmpass")
			.withImagePullPolicy(PullPolicy.defaultPolicy())
			.withReuse(true);
	
	@Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379)
            .withImagePullPolicy(PullPolicy.defaultPolicy())
            .withReuse(true);
}

