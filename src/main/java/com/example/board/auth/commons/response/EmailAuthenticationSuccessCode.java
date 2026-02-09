package com.example.board.auth.commons.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum EmailAuthenticationSuccessCode implements ApiCode {
    OTP_SENT("AUTH_EMAIL_200_001", "인증 번호가 전송되었습니다.", HttpStatus.OK),
    EMAIL_VERIFIED("AUTH_EMAIL_200_002", "이메일이 인증 완료되었습니다.", HttpStatus.OK)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    EmailAuthenticationSuccessCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
