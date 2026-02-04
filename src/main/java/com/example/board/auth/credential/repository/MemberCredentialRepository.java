package com.example.board.auth.credential.repository;

import com.example.board.auth.credential.entity.MemberCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCredentialRepository extends JpaRepository<MemberCredential, Long> {
}
