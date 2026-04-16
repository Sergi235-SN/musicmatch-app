package com.musicmatch.backend.dto;

import com.musicmatch.backend.model.ExperienceLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class ProfileSearchRequest {

    private String query;
    private Set<Long> instrumentIds;
    private Set<Long> styleIds;
    private ExperienceLevel experienceLevel;
}