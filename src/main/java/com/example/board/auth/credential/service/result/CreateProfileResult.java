package com.example.board.auth.credential.service.result;

public sealed interface CreateProfileResult {
    record Success() implements CreateProfileResult {}
    record HandleDuplicate() implements CreateProfileResult {}
    record NicknameDuplicate() implements CreateProfileResult {}
    record UnexpectedValidationError() implements CreateProfileResult {}
    record UnexpectedConflictError() implements CreateProfileResult {}
    record DownstreamServiceError() implements CreateProfileResult {}
}
