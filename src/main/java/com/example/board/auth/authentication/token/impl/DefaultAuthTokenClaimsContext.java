package com.example.board.auth.authentication.token.impl;

import com.example.board.auth.authentication.token.AuthTokenClaimsContext;
import com.example.board.auth.authentication.token.AuthTokenType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class DefaultAuthTokenClaimsContext implements AuthTokenClaimsContext {
    private final AuthTokenType tokenType;
    private final String subject;
    private final Map<String, Object> claims;
}
