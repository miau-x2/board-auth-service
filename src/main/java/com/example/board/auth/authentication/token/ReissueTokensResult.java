package com.example.board.auth.authentication.token;

import com.example.board.auth.authentication.service.result.TokenPair;

public sealed interface ReissueTokensResult {
    record Success(TokenPair tokenPair) implements ReissueTokensResult {}
    record InvalidRefreshToken() implements ReissueTokensResult {}
}
