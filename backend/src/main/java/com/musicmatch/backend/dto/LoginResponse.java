package com.musicmatch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private String username;
    private String email;
    private String token;
    private String refreshToken;
    private boolean emailVerified;
}