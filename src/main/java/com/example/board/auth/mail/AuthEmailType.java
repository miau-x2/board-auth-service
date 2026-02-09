package com.example.board.auth.mail;

import lombok.Getter;

@Getter
public enum AuthEmailType implements EmailType {
    SIGNUP("회원가입 인증번호 안내", "회원가입을 위해 이메일 인증을 진행합니다.", true),
    PASSWORD_RESET("비밀번호 재설정 인증번호 안내", "비밀번호 재설정을 위해 이메일 인증을 진행합니다.", true);

    public static final String TEMPLATE_SOURCE = "mail/auth/otp";

    private final String subject;
    private final String description;
    private final boolean html;

    AuthEmailType(String subject, String description, boolean html) {
        this.subject = subject;
        this.description = description;
        this.html = html;
    }
}
