package com.example.board.auth.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.verification")
public record EmailAuthenticationProperties(Email email, Signup signup) {
    public record Email(Otp otp) {
        public record Otp(Duration validity, Duration cooldown) {}
    }
    public record Signup(Token token) {
        public record Token(Duration validity) {}
    }
}
