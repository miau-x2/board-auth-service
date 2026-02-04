package com.example.board.auth.credential.entity;

import com.example.board.auth.credential.exception.MemberActivationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "member_credential")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCredential extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "role", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MemberRole role;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MemberStatus status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    private MemberCredential(String username, String password, String email, MemberRole role, MemberStatus status, LocalDateTime lastLoginAt, LocalDateTime withdrawnAt) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
        this.withdrawnAt = withdrawnAt;
    }

    public static MemberCredential createMember(String username, String password, String email) {
        return new MemberCredential(username, password, email, MemberRole.USER, MemberStatus.PENDING, null, null);
    }

    public static MemberCredential createAdmin(String username, String password, String email) {
        return new MemberCredential(username, password, email, MemberRole.ADMIN, MemberStatus.PENDING, null, null);
    }

    public void activate() {
        if(status != MemberStatus.PENDING) {
            throw new MemberActivationException("%s -> ACTIVE 상태로 변경할 수 없습니다.".formatted(status.name()));
        }
        status = MemberStatus.ACTIVE;
    }

    public void reactivate() {
        if(status != MemberStatus.DORMANT) {
            throw new MemberActivationException("%s -> ACTIVE 상태로 변경할 수 없습니다.".formatted(status.name()));
        }
        status = MemberStatus.ACTIVE;
    }
}