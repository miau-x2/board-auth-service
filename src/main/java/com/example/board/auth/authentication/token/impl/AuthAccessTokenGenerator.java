package com.example.board.auth.authentication.token.impl;

import com.example.board.auth.authentication.config.TokenProperties;
import com.example.board.auth.authentication.token.AuthTokenClaimsContext;
import com.example.board.auth.authentication.token.AuthTokenContext;
import com.example.board.auth.authentication.token.AuthTokenGenerator;
import com.example.board.auth.authentication.token.AuthTokenType;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthAccessTokenGenerator implements AuthTokenGenerator<AccessToken> {
    private final TokenProperties tokenProperties;
    private final PrivateKey privateKey;

    @Override
    public AccessToken generate(AuthTokenContext context) {
        if(context.getTokenType() == null) {
            return null;
        }
        if(!AuthTokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            return null;
        }
        if(!(context instanceof AuthTokenClaimsContext claimsContext)) {
            return null;
        }

        var issuedAt = Instant.now();
        var expiresAt = issuedAt.plus(tokenProperties.access().jwt().timeToLive());
        var token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(tokenProperties.access().jwt().issuer())
                .subject(claimsContext.getSubject())
                .claims(claimsContext.getClaims())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(privateKey)
                .compact();
        log.info("회원: {}의 액세스 토큰 발급. iat: {}, exp: {}", context.getSubject(), issuedAt, expiresAt);

        return new AccessToken(token, issuedAt, expiresAt);
    }
}
