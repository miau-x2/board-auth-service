package com.example.board.auth.mail.result;

public sealed interface EmailAuthenticationResult {
    sealed interface SendOtp extends EmailAuthenticationResult {
        record Success() implements SendOtp {}
        record EmailDomainNotAllowed() implements SendOtp {}
        record TooManyRequests(long retryAfterSeconds) implements SendOtp {}
    }

    sealed interface VerifyOtp extends EmailAuthenticationResult {
        record Success(String token) implements VerifyOtp {}
        record Expired() implements VerifyOtp {}
        record Invalid() implements VerifyOtp {}
    }
}
