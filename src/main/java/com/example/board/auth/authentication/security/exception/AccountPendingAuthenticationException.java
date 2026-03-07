package com.example.board.auth.authentication.security.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountPendingAuthenticationException extends AuthenticationException {
    public AccountPendingAuthenticationException(String message) {
        super(message);
    }
}
