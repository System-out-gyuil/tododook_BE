package com.gyul.tododook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TododookApplication {

	public static void main(String[] args) {
		SpringApplication.run(TododookApplication.class, args);
	}

}
