package com.example.board.auth.credential.service.result;

import com.example.board.auth.credential.entity.MemberRole;

public sealed interface GetCredentialResult {
    sealed interface Role extends GetCredentialResult {
        record Success(MemberRole role) implements Role {}
        record NotFound() implements Role {}
    }
}
