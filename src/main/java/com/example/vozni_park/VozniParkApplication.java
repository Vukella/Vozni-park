package com.example.vozni_park;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VozniParkApplication {

	public static void main(String[] args) {
		SpringApplication.run(VozniParkApplication.class, args);
	}

}
