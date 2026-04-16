package com.musicmatch.backend.controller;

import com.musicmatch.backend.dto.ProfileSearchCardDTO;
import com.musicmatch.backend.dto.ProfileSearchRequest;
import com.musicmatch.backend.service.ProfileSearchService;
import com.musicmatch.backend.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class ProfileSearchController {

    private final ProfileSearchService profileSearchService;
    private final JwtUtil jwtUtil;

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.isTokenValid(token)) {
            throw new RuntimeException("Token inválido");
        }

        return jwtUtil.extractUserId(token);
    }

    @PostMapping("/profiles")
    public List<ProfileSearchCardDTO> search(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ProfileSearchRequest request
    ) {
        Long userId = extractUserId(authHeader);

        return profileSearchService.search(userId, request);
    }
}