package com.trafficlight.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${spring.application.name:traffic-light-service}")
    private String applicationName;
    
    @Value("${server.port:9900}")
    private String serverPort;
    
    @Bean
    public OpenAPI trafficLightOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Traffic Light Controller API")
                .description("Enterprise Traffic Light Controller API for managing traffic intersections, " +
                           "traffic lights, and state transitions with real-time event processing.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Traffic Light Controller Team")
                    .email("support@trafficlight.com")
                    .url("https://trafficlight.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            .servers(Arrays.asList(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Development Server"),
                new Server()
                    .url("https://api.trafficlight.com")
                    .description("Production Server")))
            .tags(Arrays.asList(
                new Tag()
                    .name("Intersections")
                    .description("Operations for managing traffic intersections"),
                new Tag()
                    .name("Traffic Lights")
                    .description("Operations for managing individual traffic lights"),
                new Tag()
                    .name("Light Sequences")
                    .description("Operations for managing light timing sequences"),
                new Tag()
                    .name("System Health")
                    .description("System health monitoring and status endpoints"),
                new Tag()
                    .name("System Events")
                    .description("System event tracking and history"),
                new Tag()
                    .name("Analytics")
                    .description("Traffic analytics and reporting"),
                new Tag()
                    .name("Configuration")
                    .description("System configuration management")));
    }
}
