package com.example.board.auth.credential.repository.impl;

import com.example.board.auth.credential.entity.QMemberCredential;
import com.example.board.auth.credential.repository.MemberCredentialRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class MemberCredentialRepositoryImpl implements MemberCredentialRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public long updateLastLoginAt(Long id) {
        var memberCredential = QMemberCredential.memberCredential;
        return jpaQueryFactory
                .update(memberCredential)
                .set(memberCredential.lastLoginAt, LocalDateTime.now())
                .where(memberCredential.id.eq(id))
                .execute();
    }
}
