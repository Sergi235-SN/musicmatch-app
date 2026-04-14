package com.musicmatch.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.musicmatch.backend.model.Match;
import com.musicmatch.backend.model.Profile;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("""
        SELECT m FROM Match m
        WHERE (m.user1 = :p1 AND m.user2 = :p2)
           OR (m.user1 = :p2 AND m.user2 = :p1)
    """)
    Optional<Match> findBetweenProfiles(Profile p1, Profile p2);

    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END
        FROM Match m
        WHERE (m.user1 = :p1 AND m.user2 = :p2)
           OR (m.user1 = :p2 AND m.user2 = :p1)
    """)
    boolean existsBetweenProfiles(Profile p1, Profile p2);

    @Query("""
        DELETE FROM Match m
        WHERE (m.user1 = :p1 AND m.user2 = :p2)
           OR (m.user1 = :p2 AND m.user2 = :p1)
    """)
    void deleteBetweenProfiles(Profile p1, Profile p2);

    @Query("""
        SELECT m FROM Match m
        WHERE m.user1 = :profile OR m.user2 = :profile
    """)
    List<Match> findAllByProfile(Profile profile);
}
