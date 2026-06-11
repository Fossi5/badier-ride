package com.badier.badier_ride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BadierRideApplication {

	public static void main(String[] args) {
		SpringApplication.run(BadierRideApplication.class, args);
	}

}
