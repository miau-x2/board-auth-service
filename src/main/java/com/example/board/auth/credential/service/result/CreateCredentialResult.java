package com.example.board.auth.credential.service.result;

public sealed interface CreateCredentialResult {
    record Success(Long id) implements CreateCredentialResult {}
    record EmailDomainNotAllowed() implements CreateCredentialResult {}
    record TokenExpired() implements CreateCredentialResult {}
    record TokenInvalid() implements CreateCredentialResult {}
    record EmailAlreadyExists() implements CreateCredentialResult {}
    record UsernameAlreadyExists() implements CreateCredentialResult {}
}
