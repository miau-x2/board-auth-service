package com.example.board.auth.mail.result;

public sealed interface SendEmailResult {
    record Success() implements SendEmailResult {}
    record AuthenticationFailed(Throwable throwable) implements SendEmailResult {}
    record ComposeFailed(Throwable throwable) implements SendEmailResult {}
    record SendFailed(Throwable throwable) implements SendEmailResult {}
}
