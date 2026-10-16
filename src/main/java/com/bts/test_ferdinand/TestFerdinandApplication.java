package com.bts.test_ferdinand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.bts")
@EnableCaching
@EntityScan(basePackages = "com.bts.entity")
@EnableJpaRepositories(basePackages = "com.bts.repository")
public class TestFerdinandApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestFerdinandApplication.class, args);
	}

}
