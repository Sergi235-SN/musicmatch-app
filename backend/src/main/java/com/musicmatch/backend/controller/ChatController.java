package com.musicmatch.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.musicmatch.backend.dto.*;
import com.musicmatch.backend.service.MatchService;
import com.musicmatch.backend.utils.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final MatchService matchService;
    private final JwtUtil jwtUtil;

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Token inválido");
        }

        return jwtUtil.extractUserId(token);
    }

    @PostMapping("/request-or-get")
    public ChatResponse requestOrGetChat(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ChatRequest request
    ) {
        Long userId = extractUserId(authHeader);

        return matchService.requestOrGetChat(
            userId,
            request.getTargetId()
        );
    }

    @PostMapping("/{chatId}/accept")
    public void acceptChat(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long chatId
    ) {
        Long userId = extractUserId(authHeader);
        matchService.acceptChat(chatId, userId);
    }

    @PostMapping("/message")
    public void sendMessage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody MessageRequest request
    ) {
        Long userId = extractUserId(authHeader);

        matchService.sendMessage(
            request.getChatId(),
            userId,
            request.getContent()
        );
    }

    @GetMapping
    public List<ChatPreview> getMyChats(
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = extractUserId(authHeader);
        return matchService.getChatPreviews(userId);
    }

    @GetMapping("/{chatId}/messages")
    public List<MessageResponse> getMessages(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long chatId
    ) {
        Long userId = extractUserId(authHeader);

        return matchService.getMessages(chatId, userId);
    }

    @PostMapping("/{chatId}/reject")
    public void rejectChat(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long chatId
    ) {
        Long userId = extractUserId(authHeader);
        matchService.rejectChat(chatId, userId);
    }

    @GetMapping("/pending")
    public List<ChatPreview> getPendingChats(
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = extractUserId(authHeader);
        return matchService.getPendingChats(userId);
    }
}
