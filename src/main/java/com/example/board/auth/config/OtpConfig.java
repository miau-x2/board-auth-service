package com.example.board.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration(proxyBeanMethods = false)
public class OtpConfig {

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
