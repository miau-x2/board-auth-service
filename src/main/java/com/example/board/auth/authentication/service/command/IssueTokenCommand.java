package com.example.board.auth.authentication.service.command;

import com.example.board.auth.credential.entity.MemberRole;

public record IssueTokenCommand(Long memberId, MemberRole role) {
}
