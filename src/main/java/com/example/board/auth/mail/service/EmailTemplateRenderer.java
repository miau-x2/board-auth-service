package com.example.board.auth.mail.service;

import com.example.board.auth.mail.EmailType;

public interface EmailTemplateRenderer {
    String render(EmailType emailType, String otp, long expiresInMinutes);
}