package com.example.board.auth.authentication.config;

import com.example.board.auth.authentication.token.AuthToken;
import com.example.board.auth.authentication.token.AuthTokenGenerator;
import com.example.board.auth.authentication.token.impl.AuthAccessTokenGenerator;
import com.example.board.auth.authentication.token.impl.AuthRefreshTokenGenerator;
import com.example.board.auth.authentication.token.impl.DelegatingAuthTokenGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;

import java.util.Base64;

@Configuration(proxyBeanMethods = false)
public class TokenConfig {

    @Bean
    public AuthTokenGenerator<AuthToken> authTokenGenerator(AuthAccessTokenGenerator accessTokenGenerator, AuthRefreshTokenGenerator refreshTokenGenerator) {
        return new DelegatingAuthTokenGenerator(accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public StringKeyGenerator stringKeyGenerator() {
        return new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);
    }
}
