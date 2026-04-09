package com.musicmatch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String username;
    private String token;
}