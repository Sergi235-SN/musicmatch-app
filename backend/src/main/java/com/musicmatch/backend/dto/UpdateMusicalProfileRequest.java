package com.musicmatch.backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

import com.musicmatch.backend.model.ExperienceLevel;

@Getter
@Setter
public class UpdateMusicalProfileRequest {

    private ExperienceLevel experienceLevel;

    private List<Long> styleIds;

    private List<InstrumentLevelRequest> instruments;

}