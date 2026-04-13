package com.musicmatch.backend.dto;

import com.musicmatch.backend.model.ExperienceLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;

    private String biography;

    private Long cityId;
    private String cityName;

    private ExperienceLevel experienceLevel;

    private List<Long> styleIds;
    private List<InstrumentLevelResponse> instruments;

    private String profilePicture;
}