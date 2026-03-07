package com.example.board.auth.authentication.token;

public interface AuthTokenContext {
    AuthTokenType getTokenType();
    String getSubject();
}
