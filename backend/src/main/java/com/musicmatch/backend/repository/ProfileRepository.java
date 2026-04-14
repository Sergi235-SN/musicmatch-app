package com.musicmatch.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.musicmatch.backend.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @Query("""
    SELECT p FROM Profile p
    WHERE p.id != :userId
    AND p.city IS NOT NULL
    AND p.experienceLevel IS NOT NULL
    """)
    List<Profile> findCandidatesBase(Long userId);
}