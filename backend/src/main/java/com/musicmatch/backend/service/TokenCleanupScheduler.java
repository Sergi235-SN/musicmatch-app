package com.musicmatch.backend.service;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicmatch.backend.repository.EmailVerificationTokenRepository;
import com.musicmatch.backend.repository.PasswordResetTokenRepository;
import com.musicmatch.backend.repository.PendingRegistrationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional
    @Scheduled(cron = "${app.cleanup.auth-tokens-cron:0 0 * * * *}")
    public void cleanupExpiredAuthArtifacts() {
        LocalDateTime now = LocalDateTime.now();

        pendingRegistrationRepository.deleteAllByExpiresAtBefore(now);
        emailVerificationTokenRepository.deleteAllByExpiresAtBefore(now);
        passwordResetTokenRepository.deleteAllByExpiresAtBefore(now);
    }
}