package com.queueshield.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI queueShieldOpenApi() {
        return new OpenAPI().info(new Info()
                .title("QueueShield API")
                .description("Smart Emergency Resource Coordination Platform - incidents, resources, responders, shelters, and assignments")
                .version("v0.1.0")
                .contact(new Contact().name("QueueShield")));
    }
}
