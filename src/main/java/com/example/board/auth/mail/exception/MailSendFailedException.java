package com.example.board.auth.mail.exception;

public class MailSendFailedException extends RuntimeException {
    public MailSendFailedException(Throwable cause) {
        super(cause);
    }
}
