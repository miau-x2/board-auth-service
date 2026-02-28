package com.example.board.auth.commons.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberCredentialSuccessCode implements ApiCode {
    CREDENTIAL_CREATED("AUTH_CREDENTIAL_201_001", "회원 가입 완료", HttpStatus.CREATED),
    USERNAME_AVAILABILITY_CHECKED("AUTH_CREDENTIAL_200_001", "이미 사용 중인 아이디입니다.", HttpStatus.OK),
    EMAIL_AVAILABILITY_CHECKED("AUTH_CREDENTIAL_200_002", "이미 사용 중인 이메일입니다.", HttpStatus.OK)
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    MemberCredentialSuccessCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
