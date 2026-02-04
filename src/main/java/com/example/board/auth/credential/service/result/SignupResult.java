package com.example.board.auth.credential.service.result;

public sealed interface SignupResult {
    record Success() implements SignupResult {}
    record EmailDomainNotAllowed() implements SignupResult {}
    record UsernameDuplicate() implements SignupResult {}
    record EmailDuplicate() implements SignupResult {}
    record NicknameDuplicate() implements SignupResult {}
}
