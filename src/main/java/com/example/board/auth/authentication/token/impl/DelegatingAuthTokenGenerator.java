package com.example.board.auth.authentication.token.impl;

import com.example.board.auth.authentication.token.AuthToken;
import com.example.board.auth.authentication.token.AuthTokenContext;
import com.example.board.auth.authentication.token.AuthTokenGenerator;
import org.springframework.util.Assert;

import java.util.List;

public class DelegatingAuthTokenGenerator implements AuthTokenGenerator<AuthToken> {
    private final List<AuthTokenGenerator<? extends AuthToken>> tokenGenerators;

    @SafeVarargs
    public DelegatingAuthTokenGenerator(AuthTokenGenerator<? extends AuthToken>... tokenGenerators) {
        Assert.notEmpty(tokenGenerators, "tokenGenerators cannot be empty");
        Assert.noNullElements(tokenGenerators, "tokenGenerator cannot be null");
        this.tokenGenerators = List.of(tokenGenerators);
    }

    public AuthToken generate(AuthTokenContext context) {
        for (AuthTokenGenerator<? extends AuthToken> tokenGenerator : this.tokenGenerators) {
            AuthToken token = tokenGenerator.generate(context);
            if(token != null) {
                return token;
            }
        }
        return null;
    }
}
