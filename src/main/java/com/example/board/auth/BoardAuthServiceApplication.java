package com.example.board.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.resilience.annotation.EnableResilientMethods;

@ConfigurationPropertiesScan
@EnableJpaAuditing
@EnableFeignClients
@EnableDiscoveryClient
@EnableResilientMethods
@SpringBootApplication
public class BoardAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardAuthServiceApplication.class, args);
    }

}
