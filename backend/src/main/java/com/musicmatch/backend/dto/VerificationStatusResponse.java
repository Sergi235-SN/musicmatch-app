package com.musicmatch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationStatusResponse {
    private Long id;
    private String email;
    private boolean emailVerified;
    private VerificationState state;
}