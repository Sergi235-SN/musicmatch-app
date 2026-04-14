package com.musicmatch.backend.controller;

import org.springframework.web.bind.annotation.*;

import com.musicmatch.backend.dto.*;
import com.musicmatch.backend.service.MatchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/{userId}/candidates")
    public MatchCandidatesResponse getCandidates(@PathVariable Long userId) {
        return matchService.getCandidates(userId);
    }

    @PostMapping("/swipe")
    public SwipeResponse swipe(@RequestBody SwipeRequest request) {
        return matchService.swipe(
            request.getUserId(),
            request.getTargetId(),
            request.isLiked()
        );
    }

    @PostMapping("/block")
    public void block(@RequestBody BlockRequest request) {
        matchService.block(
            request.getUserId(),
            request.getTargetId()
        );
    }

    @PostMapping("/unblock")
    public void unblock(@RequestBody BlockRequest request) {
        matchService.unblock(
            request.getUserId(),
            request.getTargetId()
        );
    }

}