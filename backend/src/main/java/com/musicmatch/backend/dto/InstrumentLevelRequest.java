package com.musicmatch.backend.dto;

import lombok.Getter;
import lombok.Setter;
import com.musicmatch.backend.model.ExperienceLevel;

@Getter
@Setter
public class InstrumentLevelRequest {

    private Long instrumentId;
    private ExperienceLevel level;

}