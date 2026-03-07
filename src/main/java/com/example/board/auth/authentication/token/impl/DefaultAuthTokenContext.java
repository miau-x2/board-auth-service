package com.example.board.auth.authentication.token.impl;

import com.example.board.auth.authentication.token.AuthTokenContext;
import com.example.board.auth.authentication.token.AuthTokenType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DefaultAuthTokenContext implements AuthTokenContext {
    private final AuthTokenType tokenType;
    private final String subject;
}
