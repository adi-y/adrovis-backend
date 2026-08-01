package com.adrovis.adrovis_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AdrovisBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdrovisBackendApplication.class, args);
	}

}
