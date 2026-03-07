package com.example.board.auth.authentication.token.exception;

public class RedisConcurrencyFailureException extends RuntimeException {
    public RedisConcurrencyFailureException(String message) {
        super(message);
    }
}
