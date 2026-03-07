package com.example.board.auth.authentication.token.impl;

import java.time.Instant;

public class AccessToken extends AbstractAuthToken {
    public AccessToken(String tokenValue, Instant expiresAt) {
        this(tokenValue, null, expiresAt);
    }

    public AccessToken(String tokenValue, Instant issuedAt, Instant expiresAt) {
        super(tokenValue, issuedAt, expiresAt);
    }
}
