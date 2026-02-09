package com.example.board.auth.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(From from) {
    public record From(String address, String name) {}
}
