package com.justiceserve.hearingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HearingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HearingServiceApplication.class, args);
	}

}
