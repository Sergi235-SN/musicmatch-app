package com.musicmatch.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.musicmatch.backend.model.ExperienceLevel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateProfileRequest {
    private String biography;
    private Long cityId;
    private ExperienceLevel experienceLevel;
    private List<Long> styleIds;
    private List<InstrumentLevelRequest> instruments;
}
