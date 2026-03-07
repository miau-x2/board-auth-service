package com.example.board.auth.authentication.controller.dto.response;

import java.time.Instant;

public record ReissueResponse(
        Long memberId,
        String role,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        String tokenType
) {
    public static ReissueResponse of(
            Long memberId,
            String role,
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt) {
        return new ReissueResponse(
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
