package com.musicmatch.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "pending_registrations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_pending_registrations_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_pending_registrations_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_pending_registrations_token_hash", columnNames = "token_hash")
    }
)
@Getter
@Setter
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, name = "password_hash", length = 255)
    private String passwordHash;

    @Column(nullable = false, name = "token_hash", length = 128)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime usedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}