package com.musicmatch.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockRequest {
    private Long userId;
    private Long targetId;
}