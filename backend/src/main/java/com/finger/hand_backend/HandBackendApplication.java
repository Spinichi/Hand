package com.finger.hand_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HandBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HandBackendApplication.class, args);
	}

}
