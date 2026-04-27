package com.smartcampus.crowd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrowdIntelligenceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CrowdIntelligenceApplication.class, args);
    }
}
