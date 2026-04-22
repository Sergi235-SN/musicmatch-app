package com.musicmatch.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.musicmatch.backend.model.PendingRegistration;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

    Optional<PendingRegistration> findByEmail(String email);

    Optional<PendingRegistration> findByUsername(String username);

    Optional<PendingRegistration> findByTokenHash(String tokenHash);

    long deleteAllByExpiresAtBefore(LocalDateTime dateTime);
}