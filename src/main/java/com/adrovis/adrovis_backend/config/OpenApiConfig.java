package com.adrovis.adrovis_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.api.base-url}")
    private String baseUrl;

    @Value("${app.api.environment-label}")
    private String environmentLabel;

    @Bean
    public OpenAPI adrovisOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Adrovis Backend API")
                        .description(
                                "REST API documentation for the Adrovis backend (%s)"
                                        .formatted(environmentLabel)
                        )
                        .termsOfService("Internal use only")
                        .contact(new Contact()
                                .name("Adrovis Engineering")
                                .email("engineering@adrovis.com"))
                        .license(new License()
                                .name("Proprietary")))
                .servers(List.of(
                        new Server()
                                .url(baseUrl)
                                .description(environmentLabel + " Server")
                ));
    }
}
