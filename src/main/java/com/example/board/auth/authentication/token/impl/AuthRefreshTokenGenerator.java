package com.example.board.auth.authentication.token.impl;

import com.example.board.auth.authentication.config.TokenProperties;
import com.example.board.auth.authentication.token.AuthTokenContext;
import com.example.board.auth.authentication.token.AuthTokenGenerator;
import com.example.board.auth.authentication.token.AuthTokenType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRefreshTokenGenerator implements AuthTokenGenerator<RefreshToken> {
    private final TokenProperties tokenProperties;
    private final StringKeyGenerator stringKeyGenerator;

    @Override
    public RefreshToken generate(AuthTokenContext context) {
        if(context.getTokenType() == null) {
            return null;
        }
        if(!AuthTokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
            return null;
        }

        var issuedAt = Instant.now();
        var expiresAt = issuedAt.plus(tokenProperties.refresh().opaque().timeToLive());
        log.info("회원: {}의 리프레시 토큰 발급. iat: {}, exp: {}", context.getSubject(), issuedAt, expiresAt);

        return new RefreshToken(stringKeyGenerator.generateKey(), issuedAt, expiresAt);
    }
}
