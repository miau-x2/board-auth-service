package com.example.board.auth.commons.exception;

public class RetryableDeleteProfileException extends RetryableRemoteException {
    public RetryableDeleteProfileException(Throwable cause) {
        super(cause);
    }
}
