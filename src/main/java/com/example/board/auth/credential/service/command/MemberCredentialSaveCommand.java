package com.example.board.auth.credential.service.command;

public record MemberCredentialSaveCommand(String username, String password, String email) {
}
