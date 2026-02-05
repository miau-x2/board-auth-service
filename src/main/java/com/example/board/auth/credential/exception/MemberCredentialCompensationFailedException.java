package com.example.board.auth.credential.exception;

import lombok.Getter;

@Getter
public class MemberCredentialCompensationFailedException extends RuntimeException {
    private final Long id;
    public MemberCredentialCompensationFailedException(Long id, Throwable cause) {
        this.id = id;
        super(cause);
    }
}
