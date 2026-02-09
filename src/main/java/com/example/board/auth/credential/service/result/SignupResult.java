package com.example.board.auth.credential.service.result;

public sealed interface SignupResult {
    record Success() implements SignupResult {}
    record EmailDomainNotAllowed() implements SignupResult {}
    record TokenExpired() implements SignupResult {}
    record TokenInvalid() implements SignupResult {}
    record EmailAlreadyExists() implements SignupResult {}
    record UsernameAlreadyExists() implements SignupResult {}
    record NicknameAlreadyExists() implements SignupResult {}
    record UnexpectedValidationError() implements SignupResult {}
    record UnexpectedConflictError() implements SignupResult {}
    record DownstreamServiceError() implements SignupResult {}
    record SystemError() implements SignupResult {}
}
