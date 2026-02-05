package com.example.board.auth.credential.exception;

import lombok.Getter;

@Getter
public class MemberProfileCompensationFailedException extends RuntimeException {
    private final Long id;
    public MemberProfileCompensationFailedException(Long id) {
        this.id = id;
        super("보상 트랜잭션 회원 프로필 삭제 실패: %d, 회원 프로필과 자격 증명 삭제 필요".formatted(id));
    }
}
