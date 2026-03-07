package com.example.board.auth.authentication.service.command;

import com.example.board.auth.credential.entity.MemberRole;

public record ReissueTokenCommand(Long memberId, String refreshTokenValue, MemberRole role) {
}
