package com.example.board.auth.mail.event;

public record EmailSendEvent(String email, String otp) {
}
