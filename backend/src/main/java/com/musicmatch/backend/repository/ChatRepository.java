package com.musicmatch.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.musicmatch.backend.model.Chat;
import com.musicmatch.backend.model.ChatStatus;
import com.musicmatch.backend.model.Profile;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Chat c
        WHERE (c.user1 = :a AND c.user2 = :b)
           OR (c.user1 = :b AND c.user2 = :a)
    """)
    boolean existsBetweenProfiles(@Param("a") Profile a,
                                  @Param("b") Profile b);

    @Query("""
        SELECT c
        FROM Chat c
        WHERE (c.user1 = :a AND c.user2 = :b)
           OR (c.user1 = :b AND c.user2 = :a)
    """)
    Optional<Chat> findBetweenProfiles(@Param("a") Profile a,
                                       @Param("b") Profile b);

    
                                       
    List<Chat> findByUser1IdOrUser2Id(Long user1Id, Long user2Id);

    List<Chat> findByUser2IdAndStatus(Long user2Id, ChatStatus status);

    @Query("""
    SELECT c
    FROM Chat c
    WHERE (c.user1.id = :userId OR c.user2.id = :userId)
      AND c.status = com.musicmatch.backend.model.ChatStatus.ACTIVE
    """)
    List<Chat> findActiveChatsByUserId(@Param("userId") Long userId);
                            
}