package com.example.board.auth.authentication.token.impl;

import java.time.Instant;

public class RefreshToken extends AbstractAuthToken {
    public RefreshToken(String tokenValue, Instant expiresAt) {
        this(tokenValue, null, expiresAt);
    }

    public RefreshToken(String tokenValue, Instant issuedAt, Instant expiresAt) {
        super(tokenValue, issuedAt, expiresAt);
    }
}
