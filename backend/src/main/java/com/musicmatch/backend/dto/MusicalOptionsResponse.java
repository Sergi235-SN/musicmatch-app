package com.musicmatch.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class MusicalOptionsResponse {

    private List<?> instruments;
    private List<?> styles;

}