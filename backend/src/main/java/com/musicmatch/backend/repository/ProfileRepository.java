package com.musicmatch.backend.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.musicmatch.backend.model.ExperienceLevel;
import com.musicmatch.backend.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @Query("""
    SELECT p FROM Profile p
    WHERE p.id != :userId
    AND p.city IS NOT NULL
    AND p.experienceLevel IS NOT NULL
    """)
    List<Profile> findCandidatesBase(Long userId);

    @Query("""
        SELECT DISTINCT p
        FROM Profile p
        JOIN p.user u
        LEFT JOIN p.styles s
        LEFT JOIN p.profileInstruments pi
        LEFT JOIN pi.instrument i
        WHERE
            (:query IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')))
            AND (:experienceLevel IS NULL OR p.experienceLevel = :experienceLevel)
            AND (:styleIds IS NULL OR s.id IN :styleIds)
            AND (:instrumentIds IS NULL OR i.id IN :instrumentIds)
    """)
    List<Profile> searchProfiles(
        @Param("query") String query,
        @Param("experienceLevel") ExperienceLevel experienceLevel,
        @Param("styleIds") Set<Long> styleIds,
        @Param("instrumentIds") Set<Long> instrumentIds
    );
}