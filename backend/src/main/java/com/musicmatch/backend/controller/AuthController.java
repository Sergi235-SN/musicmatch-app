package com.musicmatch.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musicmatch.backend.dto.ApiResponse;
import com.musicmatch.backend.dto.LoginRequest;
import com.musicmatch.backend.dto.LoginResponse;
import com.musicmatch.backend.dto.RegisterRequest;
import com.musicmatch.backend.dto.UserBasicResponse;
import com.musicmatch.backend.dto.UserResponse;
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
    public ApiResponse<UserBasicResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        
        if (!jwtUtil.isTokenValid(token)) {
            return new ApiResponse<>(false, "Token inválido", null);
        }

        Long userId = jwtUtil.extractUserId(token);
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ApiResponse<>(false, "Usuario no encontrado", null);
        }

        UserBasicResponse response = new UserBasicResponse(
            user.getId(),
            user.getUsername()
        );

        return new ApiResponse<>(true, "Usuario encontrado", response);
    }
}