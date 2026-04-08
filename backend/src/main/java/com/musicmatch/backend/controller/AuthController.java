package com.musicmatch.backend.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.musicmatch.backend.dto.ApiResponse;
import com.musicmatch.backend.dto.LoginRequest;
import com.musicmatch.backend.dto.LoginResponse;
import com.musicmatch.backend.dto.RegisterRequest;
import com.musicmatch.backend.dto.UserResponse;
import com.musicmatch.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }
}