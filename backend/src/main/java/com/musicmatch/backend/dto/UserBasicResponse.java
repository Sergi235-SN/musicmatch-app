package com.musicmatch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserBasicResponse {
    private Long id;
    private String username;
}