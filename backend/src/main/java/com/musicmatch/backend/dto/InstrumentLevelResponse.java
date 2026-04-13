package com.musicmatch.backend.dto;

import com.musicmatch.backend.model.ExperienceLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InstrumentLevelResponse {

    private Long instrumentId;
    private ExperienceLevel level;

}
