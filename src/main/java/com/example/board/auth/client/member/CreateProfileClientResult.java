package com.example.board.auth.client.member;

public sealed interface CreateProfileClientResult {
    record Success() implements CreateProfileClientResult {}
    record HandleDuplicate() implements CreateProfileClientResult {}
    record NicknameDuplicate() implements CreateProfileClientResult {}
    record UnexpectedValidationError() implements CreateProfileClientResult {}
    record UnexpectedConflictError() implements CreateProfileClientResult {}
    record DownstreamServiceError() implements CreateProfileClientResult {}
}
