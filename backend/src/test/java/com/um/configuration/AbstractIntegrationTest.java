package com.um.configuration;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;


@Testcontainers
public abstract class AbstractIntegrationTest {
	
	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine")
			.withDatabaseName("taskmanager_db")
			.withUsername("tmuser")
			.withPassword("tmpass")
			.withReuse(true);
}