package com.musicmatch.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicmatch.backend.dto.*;
import com.musicmatch.backend.exception.UserBlockedException;
import com.musicmatch.backend.model.*;
import com.musicmatch.backend.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ProfileRepository profileRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ProfileBlockRepository blockRepository;

    private boolean isBlocked(Profile a, Profile b) {
        return blockRepository.existsByBlockerAndBlocked(a, b)
            || blockRepository.existsByBlockerAndBlocked(b, a);
    }

    @Transactional
    public ChatResponse requestOrGetChat(Long fromId, Long toId) {

        Profile from = profileRepository.findById(fromId).orElseThrow();
        Profile to = profileRepository.findById(toId).orElseThrow();

        if (isBlocked(from, to)) {
            throw new UserBlockedException("No puedes iniciar chat con este usuario");
        }

        Optional<Chat> existing = chatRepository.findBetweenProfiles(from, to);

        if (existing.isPresent()) {
            Chat chat = existing.get();
            return new ChatResponse(chat.getId(), chat.getStatus().name(), false);
        }

        Chat chat = new Chat();
        chat.setUser1(from);
        chat.setUser2(to);
        chat.setStatus(ChatStatus.PENDING);
        chat.setCreatedAt(LocalDateTime.now());

        chatRepository.save(chat);

        return new ChatResponse(chat.getId(), chat.getStatus().name(), true);
    }

    @Transactional
    public void acceptChat(Long chatId, Long userId) {

        Chat chat = chatRepository.findById(chatId).orElseThrow();

        if (!chat.getUser2().getId().equals(userId)) {
            throw new RuntimeException("No autorizado");
        }

        chat.setStatus(ChatStatus.ACTIVE);
    }

    @Transactional
    public void sendMessage(Long chatId, Long senderId, String content) {

        Chat chat = chatRepository.findById(chatId).orElseThrow();
        Profile sender = profileRepository.findById(senderId).orElseThrow();

        if (chat.isBlocked()) {
            throw new RuntimeException("Chat bloqueado");
        }

        if (chat.getStatus() != ChatStatus.ACTIVE) {
            throw new RuntimeException("Chat no activo");
        }

        boolean isParticipant =
            chat.getUser1().getId().equals(senderId) ||
            chat.getUser2().getId().equals(senderId);

        if (!isParticipant) {
            throw new RuntimeException("No autorizado");
        }

        Message msg = new Message();
        msg.setChat(chat);
        msg.setSender(sender);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());

        messageRepository.save(msg);
    }

    public List<ChatPreview> getChatPreviews(Long userId) {

        return chatRepository.findActiveChatsByUserId(userId)
            .stream()
            .map(c -> {

                Profile other = c.getUser1().getId().equals(userId)
                    ? c.getUser2()
                    : c.getUser1();

                String lastMessage = messageRepository
                    .findTop1ByChatIdOrderByCreatedAtDesc(c.getId())
                    .map(Message::getContent)
                    .orElse(null);

                return new ChatPreview(
                    c.getId(),
                    other.getId(),
                    other.getUser().getUsername(),
                    other.getProfilePicture(),
                    lastMessage,
                    c.getStatus().name()
                );
            })
            .toList();
    }

    public List<MessageResponse> getMessages(Long chatId, Long userId) {

        Chat chat = chatRepository.findById(chatId).orElseThrow();

        boolean isParticipant =
            chat.getUser1().getId().equals(userId) ||
            chat.getUser2().getId().equals(userId);

        if (!isParticipant) {
            throw new RuntimeException("No autorizado");
        }

        return messageRepository.findByChatIdOrderByCreatedAtAsc(chatId).stream()
            .map(m -> new MessageResponse(
                m.getId(),
                chatId,
                m.getSender().getId(),
                m.getContent()
            ))
            .toList();
    }

    @Transactional
    public void rejectChat(Long chatId, Long userId) {

        Chat chat = chatRepository.findById(chatId).orElseThrow();

        if (!chat.getUser2().getId().equals(userId)) {
            throw new RuntimeException("No autorizado");
        }

        chatRepository.delete(chat);
    }

    public List<ChatPreview> getPendingChats(Long userId) {

        return chatRepository.findByUser2IdAndStatus(
                userId,
                ChatStatus.PENDING
        ).stream()
        .map(c -> {

            Profile other = c.getUser1();

            return new ChatPreview(
                    c.getId(),
                    other.getId(),
                    other.getUser().getUsername(),
                    other.getProfilePicture(),
                    null,
                    c.getStatus().name()
            );
        })
        .toList();
    }
}