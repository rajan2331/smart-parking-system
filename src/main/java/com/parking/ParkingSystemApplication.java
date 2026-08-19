package com.parking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.parking")
public class ParkingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingSystemApplication.class, args);
	}

}
