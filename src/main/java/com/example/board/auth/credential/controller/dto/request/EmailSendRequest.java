package com.example.board.auth.credential.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

public record EmailSendRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
)
{
    public EmailSendRequest {
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
