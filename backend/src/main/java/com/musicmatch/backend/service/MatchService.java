package com.musicmatch.backend.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.musicmatch.backend.dto.*;
import com.musicmatch.backend.model.*;
import com.musicmatch.backend.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final ProfileRepository profileRepository;
    private final ProfileSwipeRepository swipeRepository;
    private final MatchRepository matchRepository;
    private final ProfileBlockRepository blockRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    private static final int DISLIKE_COOLDOWN_DAYS = 2;

    private boolean isProfileComplete(Profile p) {
        return p.getCity() != null
            && p.getExperienceLevel() != null
            && p.getStyles() != null
            && !p.getStyles().isEmpty()
            && p.getProfileInstruments() != null
            && !p.getProfileInstruments().isEmpty();
    }

    private boolean isBlocked(Profile a, Profile b) {
        return blockRepository.existsByBlockerAndBlocked(a, b)
            || blockRepository.existsByBlockerAndBlocked(b, a);
    }

    private boolean alreadyMatched(Profile a, Profile b) {
        return matchRepository.existsBetweenProfiles(a, b);
    }

    private boolean alreadyLiked(Profile a, Profile b) {
        return swipeRepository.existsByFromProfileAndToProfileAndLikedTrue(a, b);
    }
    

    private boolean recentlyDisliked(Profile a, Profile b) {
        return swipeRepository.findRecentDislike(
            a,
            b,
            LocalDateTime.now().minusDays(DISLIKE_COOLDOWN_DAYS)
        ).isPresent();
    }

    private double score(Profile a, Profile b) {

        double s = 0;

        if (a.getCity() != null && b.getCity() != null &&
            a.getCity().getId().equals(b.getCity().getId())) {
            s += 40;
        }

        if (a.getExperienceLevel() == b.getExperienceLevel()) {
            s += 20;
        }

        boolean compatibleInstrument = a.getProfileInstruments().stream()
            .anyMatch(i1 -> b.getProfileInstruments().stream()
                .anyMatch(i2 ->
                    !i1.getInstrument().getId().equals(i2.getInstrument().getId())
                    && i1.getLevel() == i2.getLevel()
                )
            );

        if (compatibleInstrument) s += 30;

        Set<Long> s1 = a.getStyles().stream().map(Style::getId).collect(Collectors.toSet());
        Set<Long> s2 = b.getStyles().stream().map(Style::getId).collect(Collectors.toSet());

        Set<Long> inter = new HashSet<>(s1);
        inter.retainAll(s2);

        Set<Long> union = new HashSet<>(s1);
        union.addAll(s2);

        s += union.isEmpty() ? 0 : ((double) inter.size() / union.size()) * 20;

        return s;
    }

    public MatchCandidatesResponse getCandidates(Long userId) {

        Profile me = profileRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!isProfileComplete(me)) {
            return new MatchCandidatesResponse(
                false,
                "Completa tu perfil para ver matches",
                List.of(),
                false
            );
        }

        List<Profile> all = profileRepository.findAll();

        List<ProfileCardDTO> result = all.stream()
            .filter(p -> !p.getId().equals(userId))
            .filter(this::isProfileComplete)
            .filter(p -> !isBlocked(me, p))
            .filter(p -> !alreadyMatched(me, p))
            .filter(p -> !alreadyLiked(me, p))
            .filter(p -> !recentlyDisliked(me, p))
            .filter(p -> score(me, p) >= 40)
            .map(p -> new ProfileCardDTO(
                p.getId(),
                p.getUser().getUsername(),
                p.getBiography(),
                p.getCity().getName(),
                p.getProfilePicture(),
                p.getStyles().stream()
                    .map(Style::getId)
                    .toList(),
                p.getProfileInstruments().stream()
                    .map(pi -> new InstrumentLevelResponse(
                        pi.getInstrument().getId(),
                        pi.getLevel()
                    ))
                    .toList(),

                score(me, p),
                p.getExperienceLevel()
            ))
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(20)
            .toList();

        if (result.isEmpty()) {
            return new MatchCandidatesResponse(
                true,
                "No hay más músicos por ahora. Vuelve más tarde o amplía tus preferencias.",
                List.of(),
                true
            );
        }

        return new MatchCandidatesResponse(
            true,
            null,
            result,
            false
        );
    }

    @Transactional
    public SwipeResponse swipe(Long userId, Long targetId, boolean liked) {

        Profile me = profileRepository.findById(userId).orElseThrow();
        Profile target = profileRepository.findById(targetId).orElseThrow();

        if (isBlocked(me, target)) {
            return new SwipeResponse(false, false, "Usuario bloqueado");
        }

        if (swipeRepository.existsByFromProfileAndToProfile(me, target)) {
            return new SwipeResponse(false, false, "Ya hiciste swipe a este usuario");
        }

        ProfileSwipe swipe = new ProfileSwipe();
        swipe.setFromProfile(me);
        swipe.setToProfile(target);
        swipe.setLiked(liked);
        swipe.setCreatedAt(LocalDateTime.now());

        swipeRepository.save(swipe);

        if (!liked) {
            return new SwipeResponse(true, false, null);
        }

        return swipeRepository
            .findByFromProfileAndToProfile(target, me)
            .filter(ProfileSwipe::isLiked)
            .map(s -> {
                createMatch(me, target);
                return new SwipeResponse(true, true, "¡Match!");
            })
            .orElse(new SwipeResponse(true, false, null));
    }

    @Transactional
    public void createMatch(Profile a, Profile b) {

        if (matchRepository.existsBetweenProfiles(a, b)) return;

        Match match = new Match();
        match.setUser1(a);
        match.setUser2(b);

        matchRepository.save(match);

        createChatFromMatch(a, b, match);
    }


    @Transactional
    public void block(Long userId, Long targetId) {

        Profile me = profileRepository.findById(userId).orElseThrow();
        Profile target = profileRepository.findById(targetId).orElseThrow();

        blockRepository.save(new ProfileBlock(me, target));

        swipeRepository.deleteByFromProfileAndToProfile(me, target);
        swipeRepository.deleteByFromProfileAndToProfile(target, me);

        matchRepository.deleteBetweenProfiles(me.getId(), target.getId());

        chatRepository.findBetweenProfiles(me, target)
            .ifPresent(chatRepository::delete);
    }

    @Transactional
    public void unblock(Long userId, Long targetId) {

        Profile me = profileRepository.findById(userId).orElseThrow();
        Profile target = profileRepository.findById(targetId).orElseThrow();

        blockRepository.deleteByBlockerAndBlocked(me, target);
    }

    private void createChatFromMatch(Profile a, Profile b, Match match) {

        Chat chat = new Chat();
        chat.setUser1(a);
        chat.setUser2(b);
        chat.setMatch(match);
        chat.setStatus(ChatStatus.ACTIVE);
        chat.setCreatedAt(LocalDateTime.now());

        chatRepository.save(chat);
    }

    @Transactional
    public ChatResponse requestOrGetChat(Long fromId, Long toId) {

        Profile from = profileRepository.findById(fromId).orElseThrow();
        Profile to = profileRepository.findById(toId).orElseThrow();

        if (isBlocked(from, to)) {
            throw new RuntimeException("Usuario bloqueado");
        }

        Optional<Chat> existing = chatRepository.findBetweenProfiles(from, to);

        if (existing.isPresent()) {
            Chat chat = existing.get();

            return new ChatResponse(
                    chat.getId(),
                    chat.getStatus().name(),
                    false
            );
        }

        Chat chat = new Chat();
        chat.setUser1(from);
        chat.setUser2(to);
        chat.setStatus(ChatStatus.PENDING);
        chat.setCreatedAt(LocalDateTime.now());

        chatRepository.save(chat);

        return new ChatResponse(
                chat.getId(),
                chat.getStatus().name(),
                true
        );
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