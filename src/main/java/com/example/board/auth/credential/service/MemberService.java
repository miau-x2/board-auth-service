package com.example.board.auth.credential.service;

import com.example.board.auth.credential.service.command.MemberSignupCommand;
import com.example.board.auth.credential.service.result.SignupResult;

public interface MemberService {
    SignupResult signup(MemberSignupCommand command);
}
