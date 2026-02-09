package com.example.board.auth.mail.result;

public sealed interface SaveOtpResult {
    sealed interface Signup extends SaveOtpResult {
        record Success(String otp) implements Signup {}
        record Cooldown(long retryAfterSeconds) implements Signup {}
    }
}
