package com.example.board.auth.authentication.service.command;

public record ReissueCommand(Long memberId, String refreshToken) {
}
