package com.example.board.auth.mail.event;

import com.example.board.auth.mail.EmailType;

public record EmailSendEvent(EmailType emailType, String email, String otp) {
}
