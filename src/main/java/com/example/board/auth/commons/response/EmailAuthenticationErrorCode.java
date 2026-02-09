package com.example.board.auth.commons.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum EmailAuthenticationErrorCode implements ApiCode {
    EMAIL_DOMAIN_NOT_ALLOWED("AUTH_EMAIL_400_001", "허용하지 않는 이메일 도메인입니다.", HttpStatus.BAD_REQUEST),
    OTP_INVALID("AUTH_EMAIL_400_002", "인증 번호가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("AUTH_EMAIL_400_003", "인증 번호가 만료되었습니다", HttpStatus.BAD_REQUEST),
    TOO_MANY_REQUESTS("AUTH_EMAIL_429_001", "이메일 재전송 요청이 초과되었습니다. 잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    EmailAuthenticationErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
