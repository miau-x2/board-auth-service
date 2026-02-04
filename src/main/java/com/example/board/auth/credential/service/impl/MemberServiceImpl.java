package com.example.board.auth.credential.service.impl;

import com.example.board.auth.credential.repository.MemberCredentialRepository;
import com.example.board.auth.credential.service.MemberService;
import com.example.board.auth.credential.service.command.MemberSignupCommand;
import com.example.board.auth.credential.service.result.SignupResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberCredentialRepository memberCredentialRepository;

    @Override
    public SignupResult signup(MemberSignupCommand command) {
        return null;
    }
}
