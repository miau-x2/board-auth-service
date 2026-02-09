package com.example.board.auth.token.impl;

import com.example.board.auth.token.TokenGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidTokenGenerator implements TokenGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
