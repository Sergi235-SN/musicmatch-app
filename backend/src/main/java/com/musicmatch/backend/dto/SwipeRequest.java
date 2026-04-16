package com.musicmatch.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwipeRequest {
    private Long targetId;
    private boolean liked;
}