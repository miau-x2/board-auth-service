package com.example.board.auth.credential.controller.dto.response;

public record SignupEmailSendResponse(long otpValiditySeconds, long cooldownSeconds) {
}
