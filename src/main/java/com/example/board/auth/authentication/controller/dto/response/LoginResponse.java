package com.example.board.auth.authentication.controller.dto.response;

import java.time.Instant;

public record LoginResponse(
        Long memberId,
        String role,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        String tokenType
) {
    public static LoginResponse of(
            Long memberId,
            String role,
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt) {
        return new LoginResponse(
                memberId,
                role,
                accessToken,
                accessTokenExpiresAt,
                refreshToken,
                refreshTokenExpiresAt,
                "Bearer"
        );
    }
}
