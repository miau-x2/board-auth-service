package com.example.board.auth.commons.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthenticationSuccessCode implements ApiCode {
    LOGIN_SUCCESS("AUTH_LOGIN_200_001", "로그인 완료", HttpStatus.OK),
    TOKEN_REISSUED("AUTH_TOKEN_200_001", "액세스 토큰 재발급 완료", HttpStatus.OK),
    LOGOUT_SUCCESS("AUTH_LOGIN_200_002", "로그아웃 완료", HttpStatus.OK);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    AuthenticationSuccessCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
