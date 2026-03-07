package com.example.board.auth.commons.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthenticationErrorCode implements ApiCode {
    BAD_CREDENTIALS("AUTH_LOGIN_401_001", "아이디 또는 비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    ACCOUNT_PENDING("AUTH_LOGIN_403_001", "유효하지 않은 계정입니다.", HttpStatus.FORBIDDEN),
    ACCOUNT_DORMANT("AUTH_LOGIN_403_002", "휴면 계정입니다.", HttpStatus.FORBIDDEN),
    ACCOUNT_WITHDRAWN("AUTH_LOGIN_403_003", "탈퇴한 계정입니다.", HttpStatus.FORBIDDEN),
    REFRESH_TOKEN_INVALID("AUTH_TOKEN_401_001", "리프레시 토큰이 만료되었거나 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_REISSUE_TEMPORARILY_UNAVAILABLE("AUTH_TOKEN_503_001", "토큰 재발급이 일시적으로 불가능합니다. 잠시 후 다시 시도해주세요.", HttpStatus.SERVICE_UNAVAILABLE)
    ;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthenticationErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
