package com.example.board.auth.mail.service.command;

public record EmailVerifyCommand(String email, String otp) {
}
