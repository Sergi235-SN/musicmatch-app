package com.musicmatch.backend.dto;

import java.util.List;

import com.musicmatch.backend.model.ExperienceLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PublicProfileResponse {
    private Long id;
    private String username;
    private String biography;
    private String cityName;
    private Long cityId;
    private String profilePicture;
    private List<Long> styleIds;
    private List<InstrumentLevelResponse> instruments;
    private ExperienceLevel experienceLevel;

    private boolean blockedByMe;
    private boolean blockedMe;

    private String chatStatus;
    private Long chatId;
}