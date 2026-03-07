package com.example.board.auth.authentication.service.result;

public sealed interface ReissueResult {
    record Success(AuthenticatedTokenPair authenticatedTokenPair) implements ReissueResult {}
    record InvalidRefreshToken() implements ReissueResult {}
}
