package com.example.board.auth.commons.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberCredentialErrorCode implements ApiCode {
    TOKEN_EXPIRED("AUTH_EMAIL_400_001", "이메일 인증이 만료되었습니다.", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID("AUTH_EMAIL_400_002", "이메일 인증이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_DOMAIN_NOT_ALLOWED("AUTH_EMAIL_400_003", "지메일과 네이버메일만 사용할 수 있습니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("AUTH_CREDENTIAL_404_001", "존재하지 않는 계정입니다.", HttpStatus.NOT_FOUND),
    USERNAME_DUPLICATED("AUTH_CREDENTIAL_409_001", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT),
    EMAIL_DUPLICATED("AUTH_CREDENTIAL_409_002", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    NICKNAME_DUPLICATED("AUTH_CREDENTIAL_409_003", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    MemberCredentialErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}