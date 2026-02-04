package com.example.board.auth.credential.service.command;

public record MemberSignupCommand(String username, String password, String email, String nickname, String token) {
}
