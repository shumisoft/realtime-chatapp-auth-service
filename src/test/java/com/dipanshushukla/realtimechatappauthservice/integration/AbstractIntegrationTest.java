package com.dipanshushukla.realtimechatappauthservice.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = {
		"spring.main.allow-bean-definition-overriding=true",
		"spring.profiles.active=test" // to avoid seeder
})
@Testcontainers
public abstract class AbstractIntegrationTest {

	@Container
	@SuppressWarnings("resource") // Supress false positive Closeable warning because Ryuk handles it
	static final MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.33"))
			.withDatabaseName("test_db")
			.withUsername("test")
			.withPassword("test");

	@Container
	@SuppressWarnings("resource")
	static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:8-alpine"))
			.withExposedPorts(6379);

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);

		registry.add("redisson.redisUrl", () -> "redis://" + redis.getHost() + ":" + redis.getFirstMappedPort());
	}

}
