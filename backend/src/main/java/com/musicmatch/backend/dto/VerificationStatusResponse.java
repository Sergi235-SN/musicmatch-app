package com.musicmatch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationStatusResponse {
    private Long userId;
    private String email;
    private boolean emailVerified;
}