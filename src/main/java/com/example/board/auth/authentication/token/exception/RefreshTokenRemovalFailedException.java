package com.example.board.auth.authentication.token.exception;

public class RefreshTokenRemovalFailedException extends RuntimeException {
    public RefreshTokenRemovalFailedException(Throwable cause) {
        super(cause);
    }
}
