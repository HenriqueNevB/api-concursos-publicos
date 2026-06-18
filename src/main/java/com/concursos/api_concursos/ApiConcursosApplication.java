package com.concursos.api_concursos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiConcursosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiConcursosApplication.class, args);
	}

}
