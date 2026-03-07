package com.example.board.auth.authentication.security;

import com.example.board.auth.credential.repository.MemberCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberCredentialRepository memberCredentialRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var memberCredential = memberCredentialRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 회원입니다."));

        return new CustomUserDetails(
                memberCredential.getId(),
                memberCredential.getUsername(),
                memberCredential.getPassword(),
                memberCredential.getRole(),
                memberCredential.getStatus()
        );
    }
}
