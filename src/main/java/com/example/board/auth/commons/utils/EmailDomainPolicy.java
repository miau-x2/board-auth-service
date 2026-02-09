package com.example.board.auth.commons.utils;

import java.util.Set;

public final class EmailDomainPolicy {
    private static final Set<String> ALLOWED_DOMAIN = Set.of("gmail.com", "naver.com");

    private EmailDomainPolicy() {}

    public static boolean isDomainAllowed(String email) {
        if(email == null) {
            return false;
        }

        var at = email.lastIndexOf("@");
        if(at <= 0 || at == email.length() - 1) {
            return false;
        }

        var domain = email.substring(at + 1);
        return ALLOWED_DOMAIN.contains(domain);
    }
}