package com.example.board.auth.authentication.security.exception;

import org.springframework.security.core.AuthenticationException;

public class AccountDormantAuthenticationException extends AuthenticationException {
    public AccountDormantAuthenticationException(String message) {
        super(message);
    }
}
