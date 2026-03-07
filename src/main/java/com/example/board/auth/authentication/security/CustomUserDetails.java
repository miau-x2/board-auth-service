package com.example.board.auth.authentication.security;

import com.example.board.auth.credential.entity.MemberRole;
import com.example.board.auth.credential.entity.MemberStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final MemberRole role;
    private final MemberStatus status;

    public CustomUserDetails(Long id, String username, String password, MemberRole role, MemberStatus status) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public @NonNull Long getId() {
        return id;
    }

    public @NonNull MemberRole getRole() {
        return role;
    }

    public @NonNull MemberStatus getStatus() {
        return status;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != MemberStatus.WITHDRAWN;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return status != MemberStatus.PENDING && status != MemberStatus.DORMANT;
    }
}
