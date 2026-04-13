package com.musicmatch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MusicalOptionsResponse {
    private List<MusicalOptionDTO> instruments;
    private List<MusicalOptionDTO> styles;
}