package com.example.board.auth.authentication.service.command;

public record LogoutCommand(Long memberId, String refreshToken) {
}
