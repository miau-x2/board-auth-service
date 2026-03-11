package com.example.board.auth.client.member;

public sealed interface DeleteProfileClientResult {
    record Success() implements DeleteProfileClientResult {}
    record DownstreamServiceError() implements DeleteProfileClientResult {}
}
