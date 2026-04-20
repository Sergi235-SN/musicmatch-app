package com.musicmatch.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musicmatch.backend.dto.ApiResponse;
import com.musicmatch.backend.dto.InstrumentLevelResponse;
import com.musicmatch.backend.dto.LoginRequest;
import com.musicmatch.backend.dto.LoginResponse;
import com.musicmatch.backend.dto.RegisterRequest;
import com.musicmatch.backend.dto.UserProfileResponse;
import com.musicmatch.backend.dto.UserResponse;
import com.musicmatch.backend.model.Profile;
import com.musicmatch.backend.model.Style;
import com.musicmatch.backend.model.User;
import com.musicmatch.backend.repository.UserRepository;
import com.musicmatch.backend.service.UserService;
import com.musicmatch.backend.utils.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");

        if (!jwtUtil.isTokenValid(token)) {
            return new ApiResponse<>(false, "Token inválido", null);
        }

        Long userId = jwtUtil.extractUserId(token);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "Usuario no encontrado", null);
        }

        Profile profile = user.getProfile();

        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getUsername(),

                profile.getBiography(),

                profile.getCity() != null ? profile.getCity().getId() : null,
                profile.getCity() != null ? profile.getCity().getName() : null,

                profile.getExperienceLevel(),

                profile.getStyles()
                        .stream()
                        .map(Style::getId)
                        .toList(),

                profile.getProfileInstruments()
                        .stream()
                        .map(pi -> new InstrumentLevelResponse(
                                pi.getInstrument().getId(),
                                pi.getLevel()
                        ))
                        .toList(),

                profile.getProfilePicture(),
                user.getEmail()
        );

        return new ApiResponse<>(true, "Usuario encontrado", response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("ok");
    }
    
}