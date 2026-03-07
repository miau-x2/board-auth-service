package com.example.board.auth.authentication.token;

import java.time.Instant;

public interface AuthToken {
    String getTokenValue();
    Instant getIssuedAt();
    Instant getExpiresAt();
}
