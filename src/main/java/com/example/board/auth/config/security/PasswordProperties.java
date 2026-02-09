package com.example.board.auth.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.authentication.user.password")
public record PasswordProperties(Bcrypt bcrypt) {
    public record Bcrypt(int strength) {}
}
