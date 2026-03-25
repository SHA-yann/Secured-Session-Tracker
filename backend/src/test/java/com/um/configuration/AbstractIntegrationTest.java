package com.um.configuration;

@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
			.withDatabaseName("taskmanager_db")
			.withUsername("tmuser")
			.withPassword("tmpass");
}