package com.example.board.auth.authentication.token;

import com.example.board.auth.authentication.token.impl.RefreshToken;

public interface RefreshTokenRepository {
    void save(Long memberId, RefreshToken refreshToken);
    void remove(Long memberId, String refreshTokenValue);
    RotateOrReplayRefreshTokenResult rotateOrReplay(Long memberId, String refreshTokenValue, RefreshToken candidateRefreshToken);
}
