package ru.otus.minioBot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import ru.otus.minioBot.model.Notification;
import ru.otus.minioBot.model.TaskComplete;
import ru.otus.minioBot.repository.NotificationsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class MinioBotApplicationTests {

	@Autowired
	NotificationsRepository repository;

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13");

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@BeforeEach
	void setUp() {
		repository.deleteAll();
		repository.save(new Notification(864770685, "130","Todo Item 1", TaskComplete.COMPLETE, LocalDate.now().atStartOfDay()));
		repository.save(new Notification( 864770685, "131","Todo Item 2", TaskComplete.COMPLETE, LocalDate.now().atStartOfDay()));
		repository.save(new Notification( 864770685, "132","Todo Item 3", TaskComplete.COMPLETE, LocalDate.now().atStartOfDay()));
	}

	@Test
	void contextLoads() {

	}

}
