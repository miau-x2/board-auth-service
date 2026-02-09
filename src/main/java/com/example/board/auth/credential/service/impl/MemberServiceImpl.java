package com.example.board.auth.credential.service.impl;

import com.example.board.auth.commons.exception.UnhandledDataIntegrityViolationException;
import com.example.board.auth.commons.utils.DatabaseConstraintName;
import com.example.board.auth.commons.utils.EmailDomainPolicy;
import com.example.board.auth.commons.utils.ExceptionUtils;
import com.example.board.auth.credential.exception.MemberActivationException;
import com.example.board.auth.credential.repository.MemberCredentialRepository;
import com.example.board.auth.credential.service.MemberService;
import com.example.board.auth.credential.service.command.MemberCredentialSaveCommand;
import com.example.board.auth.credential.service.command.MemberCredentialCreateCommand;
import com.example.board.auth.credential.service.result.ActivateCredentialResult;
import com.example.board.auth.credential.service.result.CreateCredentialResult;
import com.example.board.auth.credential.service.result.EmailAvailabilityResult;
import com.example.board.auth.credential.service.result.UsernameAvailabilityResult;
import com.example.board.auth.credential.tx.MemberCredentialTxWriter;
import com.example.board.auth.mail.repository.EmailAuthenticationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberCredentialRepository memberCredentialRepository;
    private final EmailAuthenticationRepository emailAuthenticationRepository;
    private final MemberCredentialTxWriter memberCredentialTxWriter;

    @Override
    public CreateCredentialResult createCredential(MemberCredentialCreateCommand command) {
        // 이메일 도메인 제약 조건 검증
        if(!EmailDomainPolicy.isDomainAllowed(command.email())) {
            return new CreateCredentialResult.EmailDomainNotAllowed();
        }
        // 이메일 인증 토큰 검증
        var storedEmail = emailAuthenticationRepository.useSignupToken(command.token());
        if(storedEmail == null || storedEmail.isBlank()) {
            return new CreateCredentialResult.TokenExpired();
        }

        if(!storedEmail.equals(command.email())) {
            return new CreateCredentialResult.TokenInvalid();
        }

        if(memberCredentialRepository.existsByUsername(command.username())) {
            return new CreateCredentialResult.UsernameAlreadyExists();
        }

        if(memberCredentialRepository.existsByEmail(command.email())) {
            return new CreateCredentialResult.EmailAlreadyExists();
        }

        try {
            var id = memberCredentialTxWriter.save(new MemberCredentialSaveCommand(command.username(), command.password(), command.email()));
            return new CreateCredentialResult.Success(id);
        } catch (DataIntegrityViolationException e) {
            var constraintName = ExceptionUtils.findConstraintName(e);
            if(DatabaseConstraintName.MemberCredential.EMAIL.equals(constraintName)) {
                return new CreateCredentialResult.EmailAlreadyExists();
            }
            if(DatabaseConstraintName.MemberCredential.USERNAME.equals(constraintName)) {
                return new CreateCredentialResult.UsernameAlreadyExists();
            }
            throw new UnhandledDataIntegrityViolationException(e);
        }
    }

    @Override
    @Transactional
    public ActivateCredentialResult activateCredential(Long id) {
        return memberCredentialRepository.findById(id)
                .<ActivateCredentialResult>map(credential -> {
                    try {
                        credential.activate();
                        log.info("회원 자격 증명 활성화 성공: {}", id);
                        return new ActivateCredentialResult.Success();
                    } catch (MemberActivationException _) {
                        return new ActivateCredentialResult.Failure();
                    }
                }).orElseGet(ActivateCredentialResult.NotFound::new);
    }

    @Override
    @Transactional
    public void deleteCredential(Long id) {
        memberCredentialRepository.deleteById(id);
        log.info("[SOFT] 회원 자격 증명 삭제: {}", id);
    }

    @Override
    public UsernameAvailabilityResult checkUsernameAvailability(String username) {
        if(memberCredentialRepository.existsByUsername(username)) {
            return new UsernameAvailabilityResult.Unavailable("이미 사용 중인 아이디입니다.");
        }
        return new UsernameAvailabilityResult.Available("사용 가능한 아이디입니다.");
    }

    @Override
    public EmailAvailabilityResult checkEmailAvailability(String email) {
        if(!EmailDomainPolicy.isDomainAllowed(email)) {
            return new EmailAvailabilityResult.UnAvailable("지메일과 네이버메일만 사용할 수 있습니다.");
        }
        if(memberCredentialRepository.existsByEmail(email)) {
            return new EmailAvailabilityResult.UnAvailable("이미 사용 중인 이메일입니다.");
        }
        return new EmailAvailabilityResult.Available("사용 가능한 이메일입니다.");
    }
}
