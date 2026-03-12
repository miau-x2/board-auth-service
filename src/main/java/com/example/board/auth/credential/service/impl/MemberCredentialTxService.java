package com.example.board.auth.credential.service.impl;

import com.example.board.auth.credential.entity.MemberCredential;
import com.example.board.auth.credential.repository.MemberCredentialRepository;
import com.example.board.auth.credential.service.command.MemberCredentialSaveCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCredentialTxService {
    private final MemberCredentialRepository memberCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long save(MemberCredentialSaveCommand command) {
        var credential = MemberCredential.createMember(
                command.username(), passwordEncoder.encode(command.password()), command.email()
        );
        var savedCredential = memberCredentialRepository.saveAndFlush(credential);
        log.info("회원 자격 증명 저장: {}", savedCredential.getId());
        return savedCredential.getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void hardDeleteCredential(Long id) {
        memberCredentialRepository.physicalDeleteById(id);
        log.info("[HARD] 회원 자격 증명 삭제: {}", id);
    }
}
