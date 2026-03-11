package com.example.board.auth.credential.service;

import com.example.board.auth.credential.service.command.MemberCredentialCreateCommand;
import com.example.board.auth.credential.service.result.*;

public interface MemberCredentialService {
    CreateCredentialResult createCredential(MemberCredentialCreateCommand command);
    ActivateCredentialResult activateCredential(Long id);
    void deleteCredential(Long id);
    UsernameAvailabilityResult checkUsernameAvailability(String username);
    EmailAvailabilityResult checkEmailAvailability(String email);
    GetCredentialResult.Role getMemberRole(Long id);
    long updateLastLogin(Long id);
}
