package ru.otus.minioBot;

import org.springframework.boot.SpringApplication;

public class TestMinioBotApplication {

	public static void main(String[] args) {
		SpringApplication.from(MinioBotApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
