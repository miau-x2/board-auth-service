package com.example.board.auth.authentication.token;

import com.example.board.auth.authentication.token.impl.RefreshToken;

public sealed interface RotateOrReplayRefreshTokenResult {
    record Success(RefreshToken refreshToken) implements RotateOrReplayRefreshTokenResult {}
    record Invalid() implements RotateOrReplayRefreshTokenResult {}
}
