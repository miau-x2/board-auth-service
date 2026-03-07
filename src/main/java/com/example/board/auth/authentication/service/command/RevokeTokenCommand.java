package com.example.board.auth.authentication.service.command;

public record RevokeTokenCommand(Long memberId, String refreshTokenValue) {
}
