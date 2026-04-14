package com.musicmatch.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.model.ProfileSwipe;

public interface ProfileSwipeRepository extends JpaRepository<ProfileSwipe, Long> {

    boolean existsByFromProfileAndToProfile(Profile from, Profile to);

    boolean existsByFromProfileAndToProfileAndLikedTrue(Profile from, Profile to);

    Optional<ProfileSwipe> findByFromProfileAndToProfile(Profile from, Profile to);

    @Query("""
        SELECT s FROM ProfileSwipe s
        WHERE s.fromProfile = :from
        AND s.toProfile = :to
        AND s.liked = false
        AND s.createdAt >= :since
    """)
    Optional<ProfileSwipe> findRecentDislike(
        Profile from,
        Profile to,
        LocalDateTime since
    );

    void deleteByFromProfileAndToProfile(Profile from, Profile to);
}