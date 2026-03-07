package com.example.board.auth.credential.repository;

import com.example.board.auth.credential.entity.MemberCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberCredentialRepository extends JpaRepository<MemberCredential, Long>, MemberCredentialRepositoryCustom {
    @Modifying
    @Query(value = "DELETE FROM member_credential WHERE member_id = :memberId", nativeQuery = true)
    void physicalDeleteById(@Param("memberId") Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<MemberCredential> findByUsername(String username);
}
