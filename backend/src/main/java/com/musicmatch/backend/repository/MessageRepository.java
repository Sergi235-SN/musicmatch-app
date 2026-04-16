package com.musicmatch.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.musicmatch.backend.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatIdOrderByCreatedAtAsc(Long chatId);

    Optional<Message> findTop1ByChatIdOrderByCreatedAtDesc(Long chatId);
}