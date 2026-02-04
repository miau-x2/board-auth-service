package com.example.board.auth.credential.orchestrator;

import com.example.board.auth.client.member.MemberApiClient;
import com.example.board.auth.commons.utils.DatabaseConstraintName;
import com.example.board.auth.commons.utils.EmailDomainPolicy;
import com.example.board.auth.credential.entity.MemberCredential;
import com.example.board.auth.credential.repository.MemberCredentialRepository;
import com.example.board.auth.credential.service.command.MemberSignupCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberSignupOrchestrator {
    private final MemberApiClient memberApiClient;

    public void coordinateSignup(MemberSignupCommand command) {

    }
}
