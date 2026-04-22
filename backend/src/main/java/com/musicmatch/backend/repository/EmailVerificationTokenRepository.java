package com.musicmatch.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.musicmatch.backend.model.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteAllByUser_Id(Long userId);
    long deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}