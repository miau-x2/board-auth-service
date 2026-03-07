package com.example.board.auth.authentication.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.authentication.token")
public record TokenProperties(Access access, Refresh refresh) {

    public record Access(Jwt jwt) {
        public record Jwt(String issuer, Duration timeToLive, Keys keys) {
            public record Keys(String kid, String privateKeyPem, String publicKeyPem) {}
        }
    }

    public record Refresh(Opaque opaque) {
        public record Opaque(Duration timeToLive, Duration gracePeriod) {}
    }
}