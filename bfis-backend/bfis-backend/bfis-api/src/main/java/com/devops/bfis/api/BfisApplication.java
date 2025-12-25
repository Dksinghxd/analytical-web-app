package com.devops.bfis.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * BFIS - Build Failure Intelligence System
 * 
 * Spring Boot application entry point
 * 
 * Component scanning includes all BFIS modules:
 * - com.devops.bfis.api (controllers, config)
 * - com.devops.bfis.analyzer (services, repositories)
 * - com.devops.bfis.ingestor (data seeding)
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.devops.bfis")
public class BfisApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BfisApplication.class, args);
    }
}
