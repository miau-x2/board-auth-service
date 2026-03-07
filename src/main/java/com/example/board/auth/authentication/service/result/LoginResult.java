package com.example.board.auth.authentication.service.result;

public sealed interface LoginResult {
    record Success(AuthenticatedTokenPair authenticatedTokenPair) implements LoginResult {}
    record BadCredentials() implements LoginResult {}
    record AccountPending() implements LoginResult {}
    record AccountDormant() implements LoginResult {}
    record AccountWithdrawn() implements LoginResult {}
}
