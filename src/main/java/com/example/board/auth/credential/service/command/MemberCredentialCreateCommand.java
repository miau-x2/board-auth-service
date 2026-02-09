package com.example.board.auth.credential.service.command;

public record MemberCredentialCreateCommand(String username, String password, String email, String token) {
}
