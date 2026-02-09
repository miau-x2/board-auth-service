package com.example.board.auth.credential.service.result;

public sealed interface ActivateCredentialResult {
    record Success() implements ActivateCredentialResult {}
    record NotFound() implements ActivateCredentialResult {}
    record Failure() implements ActivateCredentialResult {}
}
