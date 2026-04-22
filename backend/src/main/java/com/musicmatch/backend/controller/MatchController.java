package com.musicmatch.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.musicmatch.backend.dto.BlockRequest;
import com.musicmatch.backend.dto.BlockedUserResponse;
import com.musicmatch.backend.dto.MatchCandidatesResponse;
import com.musicmatch.backend.dto.SwipeRequest;
import com.musicmatch.backend.dto.SwipeResponse;
import com.musicmatch.backend.service.MatchService;
import com.musicmatch.backend.utils.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final JwtUtil jwtUtil;

    @GetMapping("/candidates")
    public MatchCandidatesResponse getCandidates(
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = extractUserId(authHeader);
        return matchService.getCandidates(userId);
    }

    @PostMapping("/swipe")
    public SwipeResponse swipe(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody SwipeRequest request
    ) {
        Long userId = extractUserId(authHeader);

        return matchService.swipe(
            userId,
            request.getTargetId(),
            request.isLiked()
        );
    }

    @PostMapping("/block")
    public void block(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BlockRequest request
    ) {
        Long userId = extractUserId(authHeader);

        matchService.block(
            userId,
            request.getTargetId()
        );
    }

    @PostMapping("/unblock")
    public void unblock(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody BlockRequest request
    ) {
        Long userId = extractUserId(authHeader);

        matchService.unblock(
            userId,
            request.getTargetId()
        );
    }

    @GetMapping("/blocked")
    public List<BlockedUserResponse> getBlockedUsers(
            @RequestHeader("Authorization") String authHeader
    ) {
        Long userId = extractUserId(authHeader);
        return matchService.getBlockedUsers(userId);
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Token inválido");
        }

        return jwtUtil.extractUserId(token);
    }
}