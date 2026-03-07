package com.example.board.auth.authentication.security.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountWithdrawnAuthenticationException extends AuthenticationException {
    public AccountWithdrawnAuthenticationException(String message) {
        super(message);
    }
}
