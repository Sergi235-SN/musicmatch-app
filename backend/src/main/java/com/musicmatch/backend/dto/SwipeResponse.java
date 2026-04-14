package com.musicmatch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SwipeResponse {
    private boolean success;
    private boolean match;
    private String message;
}