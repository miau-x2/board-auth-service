package com.example.board.auth.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ConfigurationPropertiesScan
@EnableJpaAuditing
@EnableDiscoveryClient
@SpringBootApplication
public class BoardAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardAuthServiceApplication.class, args);
    }

}
