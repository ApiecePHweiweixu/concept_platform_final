package com.example.concept_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ConceptPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConceptPlatformApplication.class, args);
	}

}
