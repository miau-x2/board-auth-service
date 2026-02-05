package com.example.board.auth.credential.exception;

public class MemberCredentialNotFoundException extends RuntimeException {
    public MemberCredentialNotFoundException(Long id) {
        super("존재하지 않는 회원 자격증명: %d".formatted(id));
    }
}
