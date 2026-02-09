package com.example.board.auth.credential.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record EmailVerifyRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,
        @NotBlank(message = "인증번호는 필수입니다.")
        @Size(min = 6, max = 6, message = "인증번호는 6자리입니다.")
        String otp)
{
    public EmailVerifyRequest {
        email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
