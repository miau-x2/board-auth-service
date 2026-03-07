package com.example.board.auth.authentication.service.result;

public record AuthenticatedTokenPair(Long memberId, String role, TokenPair tokenPair) {
}
