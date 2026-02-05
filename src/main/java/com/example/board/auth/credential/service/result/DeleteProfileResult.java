package com.example.board.auth.credential.service.result;

public sealed interface DeleteProfileResult {
    record Success() implements DeleteProfileResult {}
    record DownstreamServiceError() implements DeleteProfileResult {}
}
