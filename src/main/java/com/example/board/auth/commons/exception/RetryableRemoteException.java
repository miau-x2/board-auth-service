package com.example.board.auth.commons.exception;

public class RetryableRemoteException extends RuntimeException {
    public RetryableRemoteException(Throwable cause) {
        super(cause);
    }
}
