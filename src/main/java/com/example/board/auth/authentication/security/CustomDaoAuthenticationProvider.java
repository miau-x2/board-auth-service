package com.example.board.auth.authentication.security;

import com.example.board.auth.authentication.security.exception.AccountDormantAuthenticationException;
import com.example.board.auth.authentication.security.exception.AccountPendingAuthenticationException;
import com.example.board.auth.authentication.security.exception.AccountWithdrawnAuthenticationException;
import com.example.board.auth.credential.entity.MemberStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {

    public CustomDaoAuthenticationProvider(UserDetailsService userDetailsService) {
        super(userDetailsService);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
        super.additionalAuthenticationChecks(userDetails, authentication);

        var customUserDetails = (CustomUserDetails) userDetails;
        if (customUserDetails.getStatus() == MemberStatus.PENDING) {
            throw new AccountPendingAuthenticationException("계정이 PENDING 상태입니다.");
        }
        if (customUserDetails.getStatus() == MemberStatus.DORMANT) {
            throw new AccountDormantAuthenticationException("계정이 DORMANT 상태입니다.");
        }
        if (customUserDetails.getStatus() == MemberStatus.WITHDRAWN) {
            throw new AccountWithdrawnAuthenticationException("계정이 WITHDRAWN 상태입니다.");
        }
    }
}
