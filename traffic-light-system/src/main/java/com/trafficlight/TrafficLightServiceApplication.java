package com.trafficlight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Traffic Light Service Application
 * 
 * Enterprise-grade traffic light management system with:
 * - Spring Boot 3.2
 * - MySQL database with JPA/Hibernate
 * - Kafka event-driven architecture
 * - Async processing with custom thread pools
 * - Transaction management
 * - Comprehensive monitoring and logging
 * 
 * @author Traffic Light Controller Team
 * @version 1.0.0
 * @since 2024-01-27
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class TrafficLightServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrafficLightServiceApplication.class, args);
    }
}