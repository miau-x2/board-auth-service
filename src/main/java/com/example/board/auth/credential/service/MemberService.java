package com.example.board.auth.credential.service;

import com.example.board.auth.credential.service.command.MemberCredentialCreateCommand;
import com.example.board.auth.credential.service.result.ActivateCredentialResult;
import com.example.board.auth.credential.service.result.CreateCredentialResult;
import com.example.board.auth.credential.service.result.EmailAvailabilityResult;
import com.example.board.auth.credential.service.result.UsernameAvailabilityResult;

public interface MemberService {
    CreateCredentialResult createCredential(MemberCredentialCreateCommand command);
    ActivateCredentialResult activateCredential(Long id);
    void deleteCredential(Long id);
    UsernameAvailabilityResult checkUsernameAvailability(String username);
    EmailAvailabilityResult checkEmailAvailability(String email);
}
