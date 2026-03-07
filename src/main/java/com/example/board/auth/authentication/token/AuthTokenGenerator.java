package com.example.board.auth.authentication.token;

public interface AuthTokenGenerator<T extends AuthToken> {
    T generate(AuthTokenContext context);
}
