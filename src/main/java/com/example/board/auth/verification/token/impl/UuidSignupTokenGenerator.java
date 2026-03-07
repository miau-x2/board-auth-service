package com.example.board.auth.verification.token.impl;

import com.example.board.auth.verification.token.SignupTokenGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidSignupTokenGenerator implements SignupTokenGenerator {
    @Override
    public String generate() {
        return UUID.randomUUID().toString();
    }
}
