package com.example.board.auth.commons.utils;

public final class DatabaseConstraintName {
    private DatabaseConstraintName() {}

    public static final class MemberCredential {
        private MemberCredential() {}
        public static final String EMAIL = "member_credential.uk_member_credential_email";
        public static final String USERNAME = "member_credential.uk_member_credential_username";
    }
}
