package com.example.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Sample01Application {

	public static void main(String[] args) {
		SpringApplication.run(Sample01Application.class, args);
	}

}
